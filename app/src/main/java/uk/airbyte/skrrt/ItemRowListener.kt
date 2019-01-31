package uk.airbyte.skrrt

interface ItemRowListener {
    fun modifyItemState(itemObjectId: String, rapper: Rapper)
    fun onItemDelete(itemObjectId: String, rapper: Rapper)
    fun setRapperStatus(itemObjectId: String, rapper: Rapper)
    fun changeCountItemState(itemObjectId: String, rapper: Rapper)
}