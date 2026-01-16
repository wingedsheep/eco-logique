package com.wingedsheep.ecologique.inventory.worldview

import com.wingedsheep.ecologique.inventory.api.WarehouseId
import com.wingedsheep.ecologique.inventory.api.dto.StockLevel
import com.wingedsheep.ecologique.products.api.ProductId
import com.wingedsheep.ecologique.products.worldview.WorldviewProduct
import java.util.UUID

object WorldviewStock {

    data class StockEntry(
        val warehouseId: WarehouseId,
        val productId: ProductId,
        val quantityOnHand: Int
    )

    val stockEntries: List<StockEntry> = listOf(
        // Amsterdam warehouse stock
        StockEntry(
            WorldviewInventory.amsterdamWarehouse.id,
            WorldviewProduct.organicCottonTShirt.id,
            100
        ),
        StockEntry(
            WorldviewInventory.amsterdamWarehouse.id,
            WorldviewProduct.bambooToothbrushSet.id,
            250
        ),
        StockEntry(
            WorldviewInventory.amsterdamWarehouse.id,
            WorldviewProduct.solarPoweredCharger.id,
            50
        ),
        StockEntry(
            WorldviewInventory.amsterdamWarehouse.id,
            WorldviewProduct.reusableWaterBottle.id,
            150
        ),
        StockEntry(
            WorldviewInventory.amsterdamWarehouse.id,
            WorldviewProduct.organicCoffeeBeans.id,
            200
        ),
        StockEntry(
            WorldviewInventory.amsterdamWarehouse.id,
            WorldviewProduct.recycledYogaMat.id,
            30
        ),
        StockEntry(
            WorldviewInventory.amsterdamWarehouse.id,
            WorldviewProduct.miniSolarPanel.id,
            20
        ),

        // Berlin warehouse stock
        StockEntry(
            WorldviewInventory.berlinWarehouse.id,
            WorldviewProduct.organicCottonTShirt.id,
            75
        ),
        StockEntry(
            WorldviewInventory.berlinWarehouse.id,
            WorldviewProduct.bambooToothbrushSet.id,
            180
        ),
        StockEntry(
            WorldviewInventory.berlinWarehouse.id,
            WorldviewProduct.solarPoweredCharger.id,
            40
        ),
        StockEntry(
            WorldviewInventory.berlinWarehouse.id,
            WorldviewProduct.reusableWaterBottle.id,
            100
        ),
        StockEntry(
            WorldviewInventory.berlinWarehouse.id,
            WorldviewProduct.organicCoffeeBeans.id,
            150
        ),
        StockEntry(
            WorldviewInventory.berlinWarehouse.id,
            WorldviewProduct.recycledYogaMat.id,
            25
        ),
        StockEntry(
            WorldviewInventory.berlinWarehouse.id,
            WorldviewProduct.miniSolarPanel.id,
            15
        ),

        // Brussels warehouse stock
        StockEntry(
            WorldviewInventory.brusselsWarehouse.id,
            WorldviewProduct.organicCottonTShirt.id,
            60
        ),
        StockEntry(
            WorldviewInventory.brusselsWarehouse.id,
            WorldviewProduct.bambooToothbrushSet.id,
            120
        ),
        StockEntry(
            WorldviewInventory.brusselsWarehouse.id,
            WorldviewProduct.solarPoweredCharger.id,
            35
        ),
        StockEntry(
            WorldviewInventory.brusselsWarehouse.id,
            WorldviewProduct.reusableWaterBottle.id,
            80
        ),
        StockEntry(
            WorldviewInventory.brusselsWarehouse.id,
            WorldviewProduct.organicCoffeeBeans.id,
            100
        ),
        StockEntry(
            WorldviewInventory.brusselsWarehouse.id,
            WorldviewProduct.recycledYogaMat.id,
            20
        ),
        StockEntry(
            WorldviewInventory.brusselsWarehouse.id,
            WorldviewProduct.miniSolarPanel.id,
            10
        )
    )

    fun getStockForWarehouse(warehouseId: WarehouseId): List<StockEntry> =
        stockEntries.filter { it.warehouseId == warehouseId }

    fun getStockForProduct(productId: ProductId): List<StockEntry> =
        stockEntries.filter { it.productId == productId }
}
