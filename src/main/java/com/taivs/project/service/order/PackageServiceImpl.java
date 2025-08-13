package com.taivs.project.service.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.taivs.project.dto.request.*;
import com.taivs.project.dto.response.*;
import com.taivs.project.entity.*;
import com.taivs.project.entity.Package;
import com.taivs.project.exception.*;
import com.taivs.project.repository.*;
import com.taivs.project.service.inventory.InventoryService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.*;
import com.taivs.project.service.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import com.taivs.project.service.order.caching.PackageRedisService;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PackageServiceImpl implements PackageService {

    private static final List<int[]> allowedTransitions = List.of(
            new int[]{0, 3}, new int[]{3, 5}, new int[]{5, 7},
            new int[]{7, 11}, new int[]{7, 14}, new int[]{7, -1},
            new int[]{14, 11}, new int[]{14, 20}, new int[]{11, 17},
            new int[]{0, -1}, new int[]{3, -1}, new int[]{5, -1}
    );

    @Autowired private ProductRepository productRepository;
    @Autowired private PackageRepository packageRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PackageProductRepository packageProductRepository;
    @Autowired private AuthService authService;
    @Autowired private PackageRedisService packageRedisService;
    @Autowired private WarehouseRepository warehouseRepository;
    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private InventoryService inventoryService;

    private void validateForNulls(PackageProductDTO dto) {
        if (dto.getProductId() == null) {
            throw new MissingDataException("Product ID must not be null");
        }
        if (dto.getWarehouseId() == null) {
            throw new MissingDataException("Warehouse ID must not be null");
        }
        if (dto.getWeight() == null) {
            throw new MissingDataException("Weight must not be null");
        }
        if (dto.getHeight() == null) {
            throw new MissingDataException("Height must not be null");
        }
        if (dto.getLength() == null) {
            throw new MissingDataException("Length must not be null");
        }
        if (dto.getWidth() == null) {
            throw new MissingDataException("Width must not be null");
        }
        if (dto.getPrice() == null) {
            throw new MissingDataException("Price must not be null");
        }
    }

    private Product getAuthorizedProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new DataNotFoundException("Product not found: " + productId));

        Long currentUserId = authService.getCurrentUser().getId();
        if (!product.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("Unauthorized product access");
        }
        return product;
    }

    @Transactional
    private List<PackageProduct> itemizePackage(PackageDTO packageDTO, Package pack) {
        User currentUser = authService.getCurrentUser();

        List<PackageProductDTO> itemsWithIds = packageDTO.getPackageItems().stream().filter(
                p -> p.getProductId() != null
        ).toList();

        Map<String, Integer> groupedItems = new HashMap<>();
        for (PackageProductDTO item : itemsWithIds) {
            String key = item.getProductId() + "_" + item.getWarehouseId();
            groupedItems.merge(key, item.getQuantity(), Integer::sum);
        }

        Set<Long> productIds = groupedItems.keySet().stream()
                .map(k -> Long.valueOf(k.split("_")[0]))
                .collect(Collectors.toSet());

        Set<Long> warehouseIds = groupedItems.keySet().stream()
                .map(k -> Long.valueOf(k.split("_")[1]))
                .collect(Collectors.toSet());

        List<Product> products = productRepository.findAllByIdInAndUser(productIds, currentUser);
        if (products.size() != productIds.size()) {
            throw new UnauthorizedAccessException("Some products not found or unauthorized");
        }
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        List<Warehouse> warehouses = warehouseRepository.findAllByIdIn(warehouseIds.stream().toList(), currentUser.getId());
        if (warehouses.size() != warehouseIds.size()) {
            throw new UnauthorizedAccessException("Warehouses not found, deleted, or unauthorized");
        }
        Map<Long, Warehouse> warehouseMap = warehouses.stream()
                .collect(Collectors.toMap(Warehouse::getId, warehouse -> warehouse));

        List<PackageProduct> items = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : groupedItems.entrySet()) {
            String[] ids = entry.getKey().split("_");
            Long productId = Long.valueOf(ids[0]);
            Long warehouseId = Long.valueOf(ids[1]);
            Integer quantity = entry.getValue();

            items.add(PackageProduct.builder()
                    .aPackage(pack)
                    .warehouse(warehouseMap.get(warehouseId))
                    .product(productMap.get(productId))
                    .quantity(quantity)
                    .build());
        }

        List<PackageProductDTO> itemsWithoutId = packageDTO.getPackageItems().stream().filter(
                p -> !itemsWithIds.contains(p)
        ).toList();

        for (PackageProductDTO packageProductDTO : itemsWithoutId){
            this.validateForNulls(packageProductDTO);
            if (productRepository.existsByNameAndUserId(packageProductDTO.getProductName(), currentUser.getId())) throw new ResourceAlreadyExistsException("Product name already exists");
            if (productRepository.existsByBarcodeAndUserId(packageProductDTO.getBarcode(),currentUser.getId())) throw new ResourceAlreadyExistsException("Barcode already exists");
            Warehouse mainWarehouse = warehouseRepository.findMainWarehouseByUserId(currentUser.getId()).orElseThrow(
                    () -> new DataNotFoundException("Warehouse not found")
            );
            Product newProduct = Product.builder()
                    .name(packageProductDTO.getProductName())
                    .barcode(packageProductDTO.getBarcode())
                    .height(packageProductDTO.getHeight())
                    .length(packageProductDTO.getLength())
                    .weight(packageProductDTO.getWeight())
                    .width(packageProductDTO.getWidth())
                    .price(packageProductDTO.getPrice())
                    .build();

            PackageProduct packageProduct = PackageProduct.builder()
                    .warehouse(mainWarehouse)
                    .product(newProduct)
                    .quantity(packageProductDTO.getQuantity())
                    .build();
            Inventory inventory = Inventory.builder()
                    .warehouse(mainWarehouse)
                    .product(newProduct)
                    .quantity(packageProductDTO.getQuantity())
                    .user(currentUser)
                    .build();
            InventoryTransaction inventoryTransaction = InventoryTransaction.builder()
                    .warehouse(mainWarehouse)
                    .product(newProduct)
                    .quantity(packageProductDTO.getQuantity())
                    .resultingQuantity(packageProductDTO.getQuantity())
                    .type(TransactionType.IMPORT)
                    .user(currentUser)
                    .build();
            productRepository.save(newProduct);
            items.add(packageProduct);
        }

        return items;
    }

    public Double getExtraFee(List<PackageProductDTO> items) {
        double weight = 0;
        for (PackageProductDTO item : items) {
            if (item.getProductId() != null){
                Product product = this.getAuthorizedProduct(item.getProductId());
                Double w = product.getWeight();
                if (w == null) throw new DataNotFoundException("Product not found");
                weight += w*item.getQuantity();
            } else weight += item.getWeight()*item.getQuantity();
        }
        return weight <= 2 ? 0.0 : Math.round((weight - 2) * 10000 * 100.0) / 100.0;
    }
    public Double getValue(List<PackageProductDTO> items) {
        double value = 0;
        for (PackageProductDTO item : items) {
            if (item.getProductId() != null){
                Product product = this.getAuthorizedProduct(item.getProductId());
                value += product.getPrice() * item.getQuantity();
            } else value += item.getPrice() * item.getQuantity();
        }
        return Math.round(value * 100.0) / 100.0;
    }
    public Double getTotalFee(PackageDTO dto) {
        double base = dto.getPickMoney() + getExtraFee(dto.getPackageItems());
        return Math.round((dto.getShipPayer() == ShipPayer.SHOP ? base : base + 30000) * 100.0) / 100.0;
    }

    @Transactional
    public PackageResponseDTO createDraftPackage(PackageDTO dto) {
        User user = authService.getCurrentUser();

        Package newPackage = Package.builder()
                .address(dto.getAddress())
                .pickMoney(dto.getPickMoney())
                .shipMoney(30000)
                .value(getValue(dto.getPackageItems()))
                .extraFee(getExtraFee(dto.getPackageItems()))
                .totalFee(getTotalFee(dto))
                .shipPayer(dto.getShipPayer())
                .customerAddress(dto.getAddress())
                .customerTel(dto.getCustomerTel())
                .customerName(dto.getCustomerName())
                .user(user)
                .isDraft((byte) 1)
                .build();

        List<PackageProduct> items = itemizePackage(dto, newPackage);

        newPackage.setPackageItems(items);
        packageRepository.save(newPackage);

        Customer customer = customerRepository.findByTel(dto.getCustomerTel()).stream()
                .filter(c -> c.getUser().equals(user))
                .findFirst()
                .orElse(null);

        if (customer == null) {
            customer = Customer.builder()
                    .tel(dto.getCustomerTel())
                    .name(dto.getCustomerName())
                    .address(dto.getAddress())
                    .user(user)
                    .build();
        } else {
            customer.setName(dto.getCustomerName());
            customer.setAddress(dto.getAddress());
        }

        customerRepository.save(customer);
        return toResponse(newPackage,false);
    }

    @Transactional
    public PackageResponseDTO updatePackageStatus(Long id, int newStatus) {
        Package pack = packageRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Package not found"));

        if (!isValidTransition(pack.getStatus(), newStatus)) {
            throw new InvalidStatusTransitionException("Invalid status transition: " + pack.getStatus() + " -> " + newStatus);
        }

        if (pack.getIsDraft() == 1) {
            throw new InvalidDraftPackageException("Cannot update draft package");
        }

        packageRedisService.bumpUserCacheVersion(authService.getCurrentUser().getId());
        pack.setStatus(newStatus);
        packageRepository.save(pack);

        return toResponse(pack,true);
    }

    @Override
    public PagedResponse<PackageResponseDTO> searchPackages(String customerTel, Long id,
                                                            int page, int size,
                                                            String sortField, String sortDirection) {
        System.out.println("Access to service");
        if (size > 20) size = 20;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection.toUpperCase()), sortField));
        User user = authService.getCurrentUser();

        System.out.println("Prepare to get version");
        String version = packageRedisService.getUserCacheVersion(user.getId());
        String cacheKey = String.format("search::%d::%s::%d::%d::%d::%s::%s::v%s",
                user.getId(),
                customerTel != null ? customerTel : "null",
                id != null ? id : 0,
                page, size, sortField, sortDirection,
                version
        );


        System.out.println("Prepare to caching");
        Page<PackageResponseDTO> cached = packageRedisService.getCachedPackages(cacheKey, pageable);
        if (cached != null) return new PagedResponse<>(
                cached.getContent(),
                cached.getNumber(),
                cached.getSize(),
                cached.getTotalElements(),
                cached.getTotalPages(),
                cached.isLast()
        );

        Page<Package> pageResult = packageRepository.getPackages(user.getId(), customerTel, id, pageable);
        List<PackageResponseDTO> dtoList = pageResult.map(pack -> toResponse(pack,false)).getContent();

        packageRedisService.cachePackages(cacheKey, dtoList, Duration.ofMinutes(10));

        Page<PackageResponseDTO> packages =  new PageImpl<>(dtoList, pageable, pageResult.getTotalElements());
        return new PagedResponse<>(
                packages.getContent(),
                packages.getNumber(),
                packages.getSize(),
                packages.getTotalElements(),
                packages.getTotalPages(),
                packages.isLast()
        );
    }

    public PagedResponse<PackageResponseDTO> searchDraftPackages(String customerTel, Long id,int page, int size, String sortField, String sortDirection) {
        if (size > 20) size = 20;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection.toUpperCase()), sortField));
        User user = authService.getCurrentUser();
        Page<PackageResponseDTO> packageResponseDTOS =  packageRepository.findDraftPackagesByCustomerTelOrId(user.getId(),customerTel, id, pageable)
                .map(pack -> toResponse(pack,false));
        return new PagedResponse<>(
                packageResponseDTOS.getContent(),
                packageResponseDTOS.getNumber(),
                packageResponseDTOS.getSize(),
                packageResponseDTOS.getTotalElements(),
                packageResponseDTOS.getTotalPages(),
                packageResponseDTOS.isLast()
        );
    }

    private boolean isValidTransition(int from, int to) {
        return allowedTransitions.stream().anyMatch(pair -> pair[0] == from && pair[1] == to);
    }

    private PackageResponseDTO toResponse(Package pack, boolean includeUserId) {

        List<PackageProduct> packageProducts = pack.getPackageItems();
        Map<Long, Integer> groupedItems = new HashMap<>();
        for (PackageProduct  packageProduct: packageProducts ) {
            Long key = packageProduct.getProduct().getId();
            groupedItems.merge(key, packageProduct.getQuantity(), Integer::sum);
        }

        Set<Long> productIds = groupedItems.keySet();

        List<ProductResponseDTO> productResponseDTOS = productIds.stream()
                .map(id -> ProductResponseDTO.builder().id(id)
                        .name(productRepository.findById(id).get().getName())
                        .build()
                ).toList();

        List<PackageProductResponseDTO> items = productResponseDTOS.stream().map(
                productResponseDTO -> {
                    return PackageProductResponseDTO.builder().productResponseDTO(productResponseDTO)
                            .quantity(groupedItems.get(productResponseDTO.getId())).build();
                }).toList();

        PackageResponseDTO.PackageResponseDTOBuilder builder = PackageResponseDTO.builder()
                .id(pack.getId())
                .address(pack.getAddress())
                .customerName(pack.getCustomerName())
                .customerTel(pack.getCustomerTel())
                .packageItems(items)
                .value(pack.getValue())
                .extraFee(pack.getExtraFee())
                .pickMoney(pack.getPickMoney())
                .shipMoney(pack.getShipMoney())
                .shipPayer(pack.getShipPayer())
                .status(pack.getStatus())
                .isDraft(pack.getIsDraft())
                .totalFee(pack.getTotalFee());

        if (includeUserId) builder.userId(pack.getUser().getId());

        return builder.build();
    }

    @Transactional
    public PackageResponseDTO submitDraft(Long draftId) {
        User user = authService.getCurrentUser();

        Package draft = packageRepository.findById(draftId)
                .orElseThrow(() -> new DataNotFoundException("Draft not found"));

        if (draft.getIsDraft() != 1) {
            throw new InvalidDraftPackageException("Only draft packages can be submitted.");
        }

        for (PackageProduct item : draft.getPackageItems()) {
            Inventory inventory = inventoryRepository
                    .findByWarehouseIdAndProductId(item.getWarehouse().getId(),item.getProduct().getId()).orElseThrow(() -> new DataNotFoundException("Inventory not found"));

            InventoryDTO inventoryDTO = InventoryDTO.builder()
                    .productId(item.getProduct().getId())
                    .warehouseId(item.getWarehouse().getId())
                    .quantity(item.getQuantity())
                    .build();

            inventoryService.exportInventory(inventoryDTO);
        }

        draft.setIsDraft((byte) 0);

        return toResponse(packageRepository.save(draft),false);
    }

    @Transactional
    public PackageResponseDTO updateDraftPackage(Long id, PackageDTO dto) {
        Package pack = packageRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Package not found"));

        User user = authService.getCurrentUser();

        if (!Objects.equals(pack.getUser().getId(), user.getId())) {
            throw new UnauthorizedAccessException("User not authorized to update this package");
        }

        if (pack.getIsDraft() != 1) {
            throw new InvalidDraftPackageException("Only draft packages can be updated");
        }

        Customer customer = customerRepository.findByTel(dto.getCustomerTel()).stream()
                .filter(c -> c.getUser().equals(user))
                .findFirst()
                .orElse(Customer.builder().tel(dto.getCustomerTel()).user(user).build());

        customer.setName(dto.getCustomerName());
        customer.setTel(dto.getCustomerTel());
        customer.setAddress(dto.getAddress());
        customerRepository.save(customer);

        pack.setAddress(dto.getAddress());
        pack.setPickMoney(dto.getPickMoney());
        pack.setShipMoney(30000.0);
        pack.setValue(getValue(dto.getPackageItems()));
        pack.setExtraFee(getExtraFee(dto.getPackageItems()));
        pack.setTotalFee(getTotalFee(dto));
        pack.setShipPayer(dto.getShipPayer());
        pack.setCustomerTel(dto.getCustomerTel());
        pack.setCustomerName(dto.getCustomerName());
        pack.setCustomerTel(dto.getCustomerTel());

        packageProductRepository.deleteAll(pack.getPackageItems());

        List<PackageProduct> newItems = itemizePackage(dto,pack);

        pack.getPackageItems().clear();
        pack.getPackageItems().addAll(newItems);
        packageRepository.save(pack);
        return toResponse(pack,false);
    }

    public PackageResponseDTO getPackageById(Long id){
        Package aPackage = packageRepository.findById(id).orElseThrow(() -> new DataNotFoundException("Package not found"));
        User user = authService.getCurrentUser();
        boolean isAdmin = user.getUserRoles().stream().anyMatch(userRole -> "ADMIN".equals(userRole.getRole().getName()));
        if (!isAdmin && !user.equals(aPackage.getUser())) throw new UnauthorizedAccessException("Unauthorized to access");
        return toResponse(aPackage,false);
    }

    public Double getRevenue(@RequestParam String time){
        User user = authService.getCurrentUser();
        if (time.equalsIgnoreCase("today")) return packageRepository.getTodayRevenue(user.getId());
        else if (time.equalsIgnoreCase("month")) {
            return packageRepository.getThisMonthRevenue(user.getId());
        }
        else if (time.equalsIgnoreCase("year")) return packageRepository.getThisYearRevenue(user.getId());
        throw new IllegalArgumentException("Time is invalid");

    }

    @Override
    public PagedResponse<PackageResponseDTO> getPackages(Long userId,String customerTel, Long id,int page, int size, String sortField, String sortDirection) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection.toUpperCase()), sortField));
        Page<PackageResponseDTO> packageResponseDTOS = packageRepository.getPackages(userId,customerTel,id,pageable)
                .map(pack -> toResponse(pack,true));
        return new PagedResponse<>(
                packageResponseDTOS.getContent(),
                packageResponseDTOS.getNumber(),
                packageResponseDTOS.getSize(),
                packageResponseDTOS.getTotalElements(),
                packageResponseDTOS.getTotalPages(),
                packageResponseDTOS.isLast());
    }

    @Override
    @Transactional
    public void deleteDraftPackage(Long id) {
        Package pack = packageRepository.findPackageById(id).orElseThrow(() -> new DataNotFoundException("Package not found"));
        if(pack.getIsDraft() == 0) throw new InvalidDraftPackageException("Only draft package can be deleted!");
        User user = authService.getCurrentUser();
        if (pack.getUser().getId() != user.getId()) throw new UnauthorizedAccessException("Unauthorize to access this package.");
        packageRepository.deleteById(id);
    }

    @Override
    public PackageResponseDTO cancelPackage(Long id) {
        Package pack = packageRepository.findPackageById(id).orElseThrow(() -> new DataNotFoundException("Package not found"));

        User user = authService.getCurrentUser();

        if (!user.getId().equals(pack.getUser().getId())) throw new UnauthorizedAccessException("Access denied");

        if (pack.getStatus() != 0) throw new InvalidStatusTransitionException("Only can cancel package with status 0");

        pack.setStatus(-1);

        packageRepository.save(pack);

        return toResponse(pack,false);
    }

}
