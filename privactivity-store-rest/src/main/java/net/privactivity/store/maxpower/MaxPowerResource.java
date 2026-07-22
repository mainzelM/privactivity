package net.privactivity.store.maxpower;

import net.privactivity.store.service.MaxPowerService;
import net.privactivity.store.usecase.maxpower.MaxPower;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/maxpower")
public class MaxPowerResource {

    private final MaxPowerService maxPowerService;

    @Autowired
    public MaxPowerResource(MaxPowerService maxPowerService) {
        this.maxPowerService = maxPowerService;
    }

    @GetMapping("")
    public MaxPowerDTO getAll() {
        MaxPower maxPower = maxPowerService.maxPowerAll();
        return new MaxPowerDTO(maxPower);
    }


    @GetMapping("/{activityId}")
    public MaxPowerDTO get(@PathVariable long activityId) {
        MaxPower maxPower = maxPowerService.maxPowerOne(activityId);
        return new MaxPowerDTO(maxPower);
    }
}
