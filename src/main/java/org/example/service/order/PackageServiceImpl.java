package org.example.service.order;

import jakarta.transaction.Transactional;
import org.example.dto.request.PackageDTO;
import org.example.dto.request.PackageProductDTO;
import org.example.dto.request.ShipPayer;
import org.example.dto.response.CustomerLiteDTO;
import org.example.dto.response.PackageProductResponseDTO;
import org.example.dto.response.PackageResponseDTO;
import org.example.dto.response.ProductResponseDTO;
import org.example.entity.*;
import org.example.entity.Package;
import org.example.exception.*;
import org.example.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

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

    private User getCurrentUser() {
        String tel = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByTel(tel)
                .orElseThrow(() -> new UsernameNotFoundException("Tel not found: " + tel));
    }
    public Double getExtraFee(List<PackageProductDTO> items) {
        double weight = 0;
        for (PackageProductDTO item : items) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new DataNotFoundException("Product not found: " + item.getProductId()));

            User user = getCurrentUser();

            if(!product.getUser().getId().equals(user.getId())) {
                throw new UnauthorizedAccessException("Unauthorized product access");
            }

            Double w = productRepository.getWeightById(item.getProductId());
            if (w == null) throw new DataNotFoundException("Product not found: " + item.getProductId());
            weight += w;
        }
        return weight <= 2 ? 0.0 : Math.round((weight - 2) * 10000 * 100.0) / 100.0;
    }

    public Double getValue(List<PackageProductDTO> items) {
        double value = 0;
        for (PackageProductDTO item : items) {
            Product product = productRepository.findById(item.getProductId()).orElseThrow(() -> new DataNotFoundException("Product not found: " + item.getProductId()));
            User user = getCurrentUser();

            if(!product.getUser().getId().equals(user.getId())) {
                throw new UnauthorizedAccessException("Unauthorized product access");
            }

            Double p = product.getPrice();
            if (p == null) throw new DataNotFoundException("Product not found: " + item.getProductId());
            value += p;
        }
        return Math.round(value * 100.0) / 100.0;
    }

    public Double getTotalFee(PackageDTO dto) {
        double base = dto.getPickMoney() + getExtraFee(dto.getPackageItems());
        return Math.round((dto.getShipPayer() == ShipPayer.USER ? base : base + 30000) * 100.0) / 100.0;
    }

    @Transactional
    public PackageResponseDTO createDraftPackage(PackageDTO dto) {
        User user = getCurrentUser();
        Customer customer = customerRepository.findByTel(dto.getCustomerTel()).stream()
                .filter(c -> c.getUser().equals(user))
                .findFirst()
                .orElse(Customer.builder().tel(dto.getCustomerTel()).user(user).build());

        customer.setName(dto.getCustomerName());
        customerRepository.save(customer);

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

        return toResponse(newPackage);
    }



    public PackageResponseDTO updatePackageStatus(Long id, int newStatus) {
        Package pack = packageRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Package not found"));

        if (!isValidTransition(pack.getStatus(), newStatus)) {
            throw new InvalidStatusTransitionException("Invalid status transition: " + pack.getStatus() + " -> " + newStatus);
        }

        if (!Objects.equals(pack.getUser().getId(), getCurrentUser().getId())) {
            throw new UnauthorizedAccessException("Not authorized to update this package");
        }

        if (pack.getIsDraft() == 1) {
            throw new InvalidDraftPackageException("Cannot update draft package");
        }
        pack.setStatus(newStatus);
        packageRepository.save(pack);

        return toResponse(pack);
    }

    public Page<PackageResponseDTO> searchPackages(String customerTel, Long id,
                                                   int page, int size,
                                                   String sortField, String sortDirection) {
        if (size > 20) size = 20;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection.toUpperCase()), sortField));
        return packageRepository.findByCustomerTelOrId(customerTel, id, pageable)
                .map(this::toResponse);
    }

    public Page<PackageResponseDTO> searchDraftPackages(String customerTel, Long id,int page, int size, String sortField, String sortDirection) {
        if (size > 20) size = 20;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection.toUpperCase()), sortField));
        return packageRepository.findDraftPackagesByCustomerTelOrId(customerTel, id, pageable)
                .map(this::toResponse);
    }

    private boolean isValidTransition(int from, int to) {
        return allowedTransitions.stream().anyMatch(pair -> pair[0] == from && pair[1] == to);
    }

    private PackageResponseDTO toResponse(Package pack) {
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

        return PackageResponseDTO.builder()
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
                .totalFee(pack.getTotalFee())
                .build();
    }

    @Transactional
    public PackageResponseDTO submitDraft(Long draftId) {
        User user = getCurrentUser();

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

        return toResponse(packageRepository.save(draft));
    }
    @Transactional
    public PackageResponseDTO updateDraftPackage(Long id, PackageDTO dto) {
        Package pack = packageRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Package not found"));

        User user = getCurrentUser();

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
        return toResponse(pack);
    }

    public PackageResponseDTO getPackageById(Long id){
        org.example.entity.Package aPackage = packageRepository.findById(id).orElseThrow(() -> new DataNotFoundException("Package not found"));
        User user = getCurrentUser();
        if (!user.equals(aPackage.getUser())) throw new UnauthorizedAccessException("Unauthorized");
        return toResponse(aPackage);
    }

    public Double getRevenue(@RequestParam String time){
        User user = getCurrentUser();
        if (time.equalsIgnoreCase("today")) return packageRepository.getTodayRevenue(user.getId());
        else if (time.equalsIgnoreCase("month")) {
            return packageRepository.getThisMonthRevenue(user.getId());
        }
        else if (time.equalsIgnoreCase("year")) return packageRepository.getThisYearRevenue(user.getId());
        throw new IllegalArgumentException("Time is invalid");

    }
    @Override
    public PackageResponseDTO findPackageById(Long id) {
        User user = getCurrentUser();
        org.example.entity.Package order = packageRepository.findPackageById(id).orElseThrow(() -> new DataNotFoundException("Package not found"));
        Role userRole = roleRepository.findByName("USER").orElseThrow(() -> new DataNotFoundException("Role not found"));
        if(user.getUserRoles().contains(userRole)  && !Objects.equals(order.getUser().getId(), user.getId())) throw new UnauthorizedAccessException("Unauthorized");
        return toResponse(order);
    }

    @Override
    public Page<PackageResponseDTO> getAllPackages(int page, int size, String sortField, String sortDirection) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection.toUpperCase()), sortField));
        return packageRepository.findAllPackage(pageable)
                .map(this::toResponse);
    }
}
