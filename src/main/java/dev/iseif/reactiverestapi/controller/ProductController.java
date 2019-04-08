package dev.iseif.reactiverestapi.controller;

import dev.iseif.reactiverestapi.model.Product;
import dev.iseif.reactiverestapi.service.ProductService;
import java.net.URI;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/products")
public class ProductController {

  private final ProductService productService;

  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  @GetMapping
  public Flux<Product> getAllProducts() {
    return productService.getAll();
  }

  @GetMapping("{id}")
  public Mono<ResponseEntity<Product>> getProductById(@PathVariable String id) {
    return productService.getById(id)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @GetMapping("/search/{title}")
  public Flux<Product> searchByTitle(@PathVariable String title) {
    return productService.searchByTitle(title);
  }

  @PostMapping
  public Mono<ResponseEntity<Product>> createProduct(@RequestBody @Valid Product product) {
    Product productToCreate = product.toBuilder().id(null).build();
    return productService.create(productToCreate)
        .map(newProduct -> ResponseEntity.created(URI.create("/products/" + newProduct.getId())).body(newProduct));
  }

  @PutMapping("{id}")
  public Mono<ResponseEntity<Product>> updateProduct(@PathVariable String id, @RequestBody @Valid Product product) {
    return productService.update(id, product)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @DeleteMapping("{id}")
  public Mono<ResponseEntity<Void>> deleteProduct(@PathVariable String id) {
    return productService.deleteById(id)
        .map(r -> ResponseEntity.ok().<Void>build())
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }
}
