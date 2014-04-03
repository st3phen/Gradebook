package ca.uwo.csd.cs2212.team10;

import net.sf.jasperreports.engine.JRChart;
import net.sf.jasperreports.engine.JRChartCustomizer;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;

/**
 * Class to customize the charts for the PDF report
 * 
 * @author Team 10
 */
public class ChartCustomizer implements JRChartCustomizer {
    public void customize(JFreeChart chart, JRChart jrChart) {
        CategoryPlot categoryPlot = chart.getCategoryPlot();
        BarRenderer br = (BarRenderer)categoryPlot.getRenderer();
        br.setItemMargin(0);
    }
}
