package dev.iseif.reactiverestapi.service;

import dev.iseif.reactiverestapi.model.Product;
import dev.iseif.reactiverestapi.repository.ProductRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductService {

  private final ProductRepository productRepository;

  public ProductService(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  public Flux<Product> getAll() {
    return productRepository.findAll();
  }

  public Mono<Product> getById(String id) {
    return productRepository.findById(id);
  }

  public Flux<Product> searchByTitle(String title) {
    return productRepository.findByTitleContainingIgnoreCase(title);
  }

  public Mono<Product> create(Product product) {
    return productRepository.save(product);
  }

  public Mono<Product> update(String id, Product updatedProduct) {
    return productRepository.findById(id)
        .map(existingProduct -> existingProduct.toBuilder()
              .title(updatedProduct.getTitle())
              .description(updatedProduct.getDescription())
              .price(updatedProduct.getPrice())
              .build())
        .flatMap(productRepository::save);
  }

  public Mono<Product> deleteById(String id) {
    return productRepository.findById(id)
        .flatMap(product -> productRepository.delete(product).then(Mono.just(product)));
  }
}
