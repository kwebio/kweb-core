package io.kweb.plugins.chartJs

import io.kweb.*
import io.kweb.dom.element.creation.tags.CanvasElement
import java.time.Instant

class Chart(canvas: CanvasElement, chartConfig: ChartConfig) {
    private val chartVarName = "c${random.nextInt(10000000)}"

    init {
        canvas.creator?.require(ChartJsPlugin::class)
        canvas.execute("""
            var $chartVarName = new Chart(${canvas.jsExpression}.getContext('2d'), ${chartConfig.toJson()})
        """.trimIndent())
    }


}

data class ChartConfig(val type: ChartType, val data: ChartData)


enum class ChartType {
    bar, line
}

data class ChartData(val labels: List<String>,
                     val datasets: List<DataSet>)

class DataSet(val label: String,
                   dataList: DataList,
              val type : ChartType? = null) {
    val data : Array<out Any> = dataList.list
}

data class Point(val x : Number, val y : Number)
data class DatePoint(val x : Instant, val y : Number)

sealed class DataList(val list : Array<out Any>) {
    class Numbers(vararg numbers : Number) : DataList(numbers)
    class Points(vararg points : Point) : DataList(points)
}

