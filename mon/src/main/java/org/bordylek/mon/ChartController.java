package org.bordylek.mon;

import org.bordylek.mon.model.Chart;
import org.bordylek.mon.repository.ChartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
public class ChartController {

	@Autowired
	private ChartRepository chartRepository;

	private static final Logger LOG = LoggerFactory.getLogger(ChartController.class);

	@RequestMapping(value = "/charts", method = RequestMethod.GET, produces = "application/json")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@Cacheable("charts")
	public List<Chart> getCharts() {
		return chartRepository.findAll();
	}

    @RequestMapping(value = "/charts/series", method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Cacheable("charts")
    public List<Chart> getSeries() {
        return chartRepository.findAll();
    }

    @RequestMapping(value = "/charts", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
    @CacheEvict(value = "charts", allEntries = true)
	public void createChart(@RequestBody @Valid Chart chart) {
		chartRepository.save(chart);
	}

}
