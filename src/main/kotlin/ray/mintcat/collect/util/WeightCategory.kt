package ray.mintcat.collect.util

//战士教我的权重模块
class WeightCategory<T>(
    private var category: T,
    private var weight: Int
) {

    fun weightCategory(category: T, weight: Int) {
        this.setCategory(category)
        this.setWeight(weight)
    }

    fun getWeight(): Int {
        return weight
    }

    fun setWeight(weight: Int) {
        this.weight = weight
    }

    fun getCategory(): T {
        return category
    }

    fun setCategory(category: T) {
        this.category = category
    }

}