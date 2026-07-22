package net.privactivity.store.aggregator;

import net.privactivity.store.service.AggregatorService;
import net.privactivity.store.usecase.getaggregation.model.TotalAggregation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Arrays;

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
        int eddingtonNumber = aggregatorService.eddingtonNumber();
        if (monthsAsString.isEmpty()) {
            TotalAggregation totalAggregation = aggregatorService.getTotalAggregation();
            return new TotalAggregationDTO(totalAggregation, eddingtonNumber);
        } else {
            int[] monthArray = Arrays.stream(monthsAsString.split(","))
                                     .mapToInt(Integer::parseInt)
                                     .toArray();
            TotalAggregation totalAggregation =
                    aggregatorService.aggregateForMonths(monthArray);
            return new TotalAggregationDTO(totalAggregation, eddingtonNumber);
        }
    }

    @GetMapping("ytd")
    TotalAggregationDTO getYTD() {
        TotalAggregation totalAggregation = aggregatorService.aggregateYTD();
        int eddingtonNumber = aggregatorService.eddingtonNumber();
        return new TotalAggregationDTO(totalAggregation, eddingtonNumber);
    }
}
