package com.taivs.project.service.order;

import com.taivs.project.entity.*;
import com.taivs.project.entity.Package;
import com.taivs.project.exception.*;
import com.taivs.project.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.*;
import com.taivs.project.dto.request.PackageDTO;
import com.taivs.project.dto.request.PackageProductDTO;
import com.taivs.project.dto.request.ShipPayer;
import com.taivs.project.dto.response.CustomerLiteDTO;
import com.taivs.project.dto.response.PackageProductResponseDTO;
import com.taivs.project.dto.response.PackageResponseDTO;
import com.taivs.project.dto.response.ProductResponseDTO;
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
    @Autowired private RoleRepository roleRepository;
    @Autowired private AuthService authService;
    @Autowired private PackageRedisService packageRedisService;

    public Double getExtraFee(List<PackageProductDTO> items) {
        double weight = 0;
        for (PackageProductDTO item : items) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new DataNotFoundException("Product not found: " + item.getProductId()));

            User user = authService.getCurrentUser();

            if(!product.getUser().getId().equals(user.getId())) {
                throw new UnauthorizedAccessException("Unauthorized product access");
            }

            Double w = productRepository.getWeightById(item.getProductId());
            if (w == null) throw new DataNotFoundException("Product not found: " + item.getProductId());
            weight += w*item.getQuantity();
        }
        return weight <= 2 ? 0.0 : Math.round((weight - 2) * 10000 * 100.0) / 100.0;
    }
    public Double getValue(List<PackageProductDTO> items) {
        double value = 0;
        for (PackageProductDTO item : items) {
            Product product = productRepository.findById(item.getProductId()).orElseThrow(() -> new DataNotFoundException("Product not found: " + item.getProductId()));
            User user = authService.getCurrentUser();

            if(!product.getUser().getId().equals(user.getId())) {
                throw new UnauthorizedAccessException("Unauthorized product access");
            }

            Double p = product.getPrice();
            if (p == null) throw new DataNotFoundException("Product not found: " + item.getProductId());
            value += p * item.getQuantity();
        }
        return Math.round(value * 100.0) / 100.0;
    }
    public Double getTotalFee(PackageDTO dto) {
        double base = dto.getPickMoney() + getExtraFee(dto.getPackageItems());
        return Math.round((dto.getShipPayer() == ShipPayer.USER ? base : base + 30000) * 100.0) / 100.0;
    }

    @Transactional
    public PackageResponseDTO createDraftPackage(PackageDTO dto) {
        User user = authService.getCurrentUser();
        Customer customer = customerRepository.findByTel(dto.getCustomerTel()).stream()
                .filter(c -> c.getUser().equals(user))
                .findFirst()
                .orElse(null);

        if (customer == null) {
            customer = Customer.builder()
                    .tel(dto.getCustomerTel())
                    .name(dto.getCustomerName())
                    .user(user)
                    .build();
        } else {
            customer.setName(dto.getCustomerName());
        }

        customer = customerRepository.save(customer);
        Package newPackage = Package.builder()
                .address(dto.getAddress())
                .pickMoney(dto.getPickMoney())
                .shipMoney(30000)
                .value(getValue(dto.getPackageItems()))
                .extraFee(getExtraFee(dto.getPackageItems()))
                .totalFee(getTotalFee(dto))
                .shipPayer(dto.getShipPayer())
                .customer(customer)
                .user(user)
                .isDraft((byte) 1)
                .build();
        Map<Long, Integer> groupedItems = new HashMap<>();
        for (PackageProductDTO item : dto.getPackageItems()) {
            groupedItems.merge(item.getProductId(), item.getQuantity(), Integer::sum);
        }
        List<PackageProduct> items = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : groupedItems.entrySet()) {
            Long productId = entry.getKey();
            int quantity = entry.getValue();

            Product p = productRepository.findById(productId)
                    .orElseThrow(() -> new DataNotFoundException("Product not found: " + productId));

            if (!p.getUser().equals(user)) throw new UnauthorizedAccessException("Unauthorized product access");

            items.add(PackageProduct.builder()
                    .aPackage(newPackage)
                    .product(p)
                    .quantity(quantity)
                    .build());
        }

        packageProductRepository.saveAll(items);
        newPackage.setPackageItems(items);
        packageRepository.save(newPackage);

        return toResponse(newPackage,false);
    }

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
    public Page<PackageResponseDTO> searchPackages(String customerTel, Long id,
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
        if (cached != null) return cached;

        Page<Package> pageResult = packageRepository.getPackages(user.getId(), customerTel, id, pageable);
        List<PackageResponseDTO> dtoList = pageResult.map(pack -> toResponse(pack,false)).getContent();

        System.out.println("Caching into Redis");
        packageRedisService.cachePackages(cacheKey, dtoList, Duration.ofMinutes(10));

        return new PageImpl<>(dtoList, pageable, pageResult.getTotalElements());
    }

    public Page<PackageResponseDTO> searchDraftPackages(String customerTel, Long id,int page, int size, String sortField, String sortDirection) {
        if (size > 20) size = 20;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection.toUpperCase()), sortField));
        User user = authService.getCurrentUser();
        return packageRepository.findDraftPackagesByCustomerTelOrId(user.getId(),customerTel, id, pageable)
                .map(pack -> toResponse(pack,false));
    }

    private boolean isValidTransition(int from, int to) {
        return allowedTransitions.stream().anyMatch(pair -> pair[0] == from && pair[1] == to);
    }

    private PackageResponseDTO toResponse(Package pack, boolean includeUserId) {
        CustomerLiteDTO customerDTO = CustomerLiteDTO.builder()
                .id(pack.getCustomer().getId())
                .name(pack.getCustomer().getName())
                .tel(pack.getCustomer().getTel())
                .build();

        List<PackageProductResponseDTO> items = pack.getPackageItems().stream()
                .map(item -> PackageProductResponseDTO.builder()
                        .id(item.getId())
                        .quantity(item.getQuantity())
                        .productResponseDTO(ProductResponseDTO.builder()
                                .id(item.getProduct().getId())
                                .name(item.getProduct().getName())
                                .barcode(item.getProduct().getBarcode())
                                .price(item.getProduct().getPrice())
                                .weight(item.getProduct().getWeight())
                                .width(item.getProduct().getWidth())
                                .height(item.getProduct().getHeight())
                                .length(item.getProduct().getLength())
                                .stock(item.getProduct().getStock())
                                .imageUrls(item.getProduct().getProductImages().stream().map(ProductImage::getImageUrl).toList())
                                .build())
                        .build())
                .toList();

        PackageResponseDTO.PackageResponseDTOBuilder builder = PackageResponseDTO.builder()
                .id(pack.getId())
                .address(pack.getAddress())
                .customer(customerDTO)
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
            Product product = item.getProduct();
            int quantity = item.getQuantity();

            if (!product.getUser().equals(user)) {
                throw new UnauthorizedAccessException("Unauthorized access to product " + product.getId());
            }

            if (quantity > product.getStock()) {
                throw new NotEnoughStockException("Not enough stock for product " + product.getId());
            }

            product.setStock(product.getStock() - quantity);
            productRepository.save(product);
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
        customerRepository.save(customer);

        pack.setAddress(dto.getAddress());
        pack.setPickMoney(dto.getPickMoney());
        pack.setShipMoney(30000.0);
        pack.setValue(getValue(dto.getPackageItems()));
        pack.setExtraFee(getExtraFee(dto.getPackageItems()));
        pack.setTotalFee(getTotalFee(dto));
        pack.setShipPayer(dto.getShipPayer());
        pack.setCustomer(customer);

        packageProductRepository.deleteAll(pack.getPackageItems());

        List<PackageProduct> newItems = dto.getPackageItems().stream()
                .map(item -> {
                    Product product = productRepository.findById(item.getProductId())
                            .orElseThrow(() -> new DataNotFoundException("Product not found: " + item.getProductId()));
                    if (!Objects.equals(user.getId(), product.getUser().getId())) {
                        throw new UnauthorizedAccessException("Unauthorized access to product " + product.getId());
                    }

                    return PackageProduct.builder()
                            .aPackage(pack)
                            .product(product)
                            .quantity(item.getQuantity())
                            .build();
                })
                .collect(Collectors.toList());

        packageProductRepository.saveAll(newItems);
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
    public PackageResponseDTO findPackageById(Long id) {
        User user = authService.getCurrentUser();
        Package order = packageRepository.findPackageById(id).orElseThrow(() -> new DataNotFoundException("Package not found"));
        Role userRole = roleRepository.findByName("USER").orElseThrow(() -> new DataNotFoundException("Role not found"));
        if(user.getUserRoles().contains(userRole)  && !Objects.equals(order.getUser().getId(), user.getId())) throw new UnauthorizedAccessException("Unauthorized");
        return toResponse(order,false);
    }

    @Override
    public Page<PackageResponseDTO> getPackages(Long userId,String customerTel, Long id,int page, int size, String sortField, String sortDirection) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection.toUpperCase()), sortField));
        return packageRepository.getPackages(userId,customerTel,id,pageable)
                .map(pack -> toResponse(pack,true));
    }

    @Override
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
