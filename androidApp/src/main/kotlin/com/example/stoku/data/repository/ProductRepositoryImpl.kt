package com.example.stoku.data.repository

import com.example.stoku.data.local.dao.ProductDao
import com.example.stoku.data.local.entity.ProductEntity
import com.example.stoku.domain.model.Product
import com.example.stoku.domain.repository.ProductRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,
) : ProductRepository {
    override fun getAll(): Flow<List<Product>> =
        productDao.getAll().map { products -> products.map { it.toDomain() } }

    override fun getLowStock(): Flow<List<Product>> =
        productDao.getLowStock().map { products -> products.map { it.toDomain() } }

    override fun search(query: String): Flow<List<Product>> =
        productDao.search(query).map { products -> products.map { it.toDomain() } }

    override suspend fun getBySku(sku: String): Product? = productDao.getBySku(sku)?.toDomain()

    override suspend fun insert(product: Product) = productDao.insert(product.toEntity())

    override suspend fun update(product: Product) = productDao.update(product.toEntity())

    override suspend fun adjustStock(sku: String, delta: Int, updatedAt: Long) =
        productDao.adjustStock(sku, delta, updatedAt)
}

private fun ProductEntity.toDomain(): Product = Product(
    sku = sku,
    brandName = brandName,
    productName = productName,
    category = category,
    stock = stock,
    costPrice = costPrice,
    sellingPrice = sellingPrice,
    lowStockThreshold = lowStockThreshold,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

private fun Product.toEntity(): ProductEntity = ProductEntity(
    sku = sku,
    brandName = brandName,
    productName = productName,
    category = category,
    stock = stock,
    costPrice = requireNotNull(costPrice) { "costPrice must not be null when persisting a product" },
    sellingPrice = sellingPrice,
    lowStockThreshold = lowStockThreshold,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
