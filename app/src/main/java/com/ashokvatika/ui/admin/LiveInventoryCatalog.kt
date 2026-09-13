package com.ashokvatika.ui.admin

import com.ashokvatika.domain.model.DashboardDestination
import com.ashokvatika.domain.model.InventoryItem

object LiveInventoryCatalog {
  var inventoryByDestination: Map<DashboardDestination, List<InventoryItem>> = emptyMap()
}
