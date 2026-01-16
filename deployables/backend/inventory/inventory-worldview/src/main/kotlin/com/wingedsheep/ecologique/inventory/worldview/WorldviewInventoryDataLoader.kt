package com.wingedsheep.ecologique.inventory.worldview

import com.wingedsheep.ecologique.inventory.api.WarehouseService
import com.wingedsheep.ecologique.inventory.api.dto.StockUpdateRequest
import com.wingedsheep.ecologique.inventory.api.dto.WarehouseCreateRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(2) // After products are loaded
class WorldviewInventoryDataLoader(
    private val warehouseService: WarehouseService,
    @Value("\${spring.profiles.active:}") private val activeProfile: String
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        if (activeProfile.contains("prod") || activeProfile.contains("test")) {
            logger.info("Skipping worldview inventory data for profile: $activeProfile")
            return
        }

        logger.info("Loading worldview inventory data...")
        loadWarehouses()
        loadStock()
        logger.info("Worldview inventory data loaded successfully")
    }

    private fun loadWarehouses() {
        val existingWarehouses = warehouseService.getAllWarehouses()
            .getOrNull()
            ?.associateBy { it.name }
            ?: emptyMap()

        WorldviewInventory.allWarehouses.forEach { warehouse ->
            if (existingWarehouses.containsKey(warehouse.name)) {
                logger.debug("Worldview warehouse already exists: ${warehouse.name}")
                return@forEach
            }

            val request = WarehouseCreateRequest(
                name = warehouse.name,
                countryCode = warehouse.countryCode,
                address = warehouse.address
            )

            warehouseService.createWarehouse(request)
                .onSuccess { created ->
                    logger.debug("Created worldview warehouse: ${created.name} (${created.id.value})")
                }
                .onFailure { error ->
                    logger.warn("Failed to create worldview warehouse ${warehouse.name}: $error")
                }
        }
    }

    private fun loadStock() {
        val warehouses = warehouseService.getAllWarehouses()
            .getOrNull()
            ?.associateBy { it.name }
            ?: emptyMap()

        WorldviewInventory.allWarehouses.forEach { worldviewWarehouse ->
            val warehouse = warehouses[worldviewWarehouse.name] ?: return@forEach

            val stockEntries = WorldviewStock.getStockForWarehouse(worldviewWarehouse.id)
            stockEntries.forEach { entry ->
                val request = StockUpdateRequest(
                    productId = entry.productId,
                    quantityOnHand = entry.quantityOnHand
                )

                warehouseService.updateStock(warehouse.id, request)
                    .onSuccess {
                        logger.debug("Set stock for ${entry.productId.value} in ${warehouse.name}: ${entry.quantityOnHand}")
                    }
                    .onFailure { error ->
                        logger.warn("Failed to set stock for ${entry.productId.value} in ${warehouse.name}: $error")
                    }
            }
        }
    }
}
