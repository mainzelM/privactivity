package net.privactivity.store.aggregator;

import net.privactivity.store.service.AggregatorService;
import net.privactivity.store.usecase.getaggregation.model.TotalAggregation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/aggregations")
public class AggregationsResource {


    private final AggregatorService aggregatorService;

    @Autowired
    public AggregationsResource(AggregatorService aggregatorService) {
        this.aggregatorService = aggregatorService;
    }

    @GetMapping("")
    TotalAggregationDTO getAll(@RequestParam(value = "months", defaultValue = "") String monthsAsString) {
        if (monthsAsString.isEmpty()) {
            TotalAggregation totalAggregation = aggregatorService.getTotalAggregation();
            return new TotalAggregationDTO(totalAggregation);
        } else {
            int[] monthArray = Arrays.stream(monthsAsString.split(","))
                                     .mapToInt(Integer::parseInt)
                                     .toArray();
            TotalAggregation totalAggregation =
                    aggregatorService.aggregateForMonths(monthArray);
            return new TotalAggregationDTO(totalAggregation);
        }
    }

    @GetMapping("ytd")
    TotalAggregationDTO getYTD() {
        TotalAggregation totalAggregation = aggregatorService.aggregateYTD();
        return new TotalAggregationDTO(totalAggregation);
    }

    @GetMapping("moving-sum")
    public Map<LocalDate, MovingSumDTO> movingSum(@RequestParam(value = "numDays", defaultValue = "365") int numDays) {
        Map<LocalDate, MovingSumDTO> result = new HashMap<>();
        aggregatorService.movingSum(numDays).forEach((date, movingSum) -> result.put(date, new MovingSumDTO(movingSum)));
        return result;
    }

    @GetMapping("eddington-chart")
    public EddigtionChartDTO eddingtonChart() {
        return new EddigtionChartDTO(aggregatorService.eddingtionChart());
    }
}
