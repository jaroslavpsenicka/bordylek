package org.bordylek.mon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bordylek.mon.model.Chart;
import org.bordylek.mon.repository.ChartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
public class ChartController {

	@Autowired
	private ChartRepository chartRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

	private static final Logger LOG = LoggerFactory.getLogger(ChartController.class);

	@RequestMapping(value = "/charts", method = RequestMethod.GET, produces = "application/json")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@Cacheable("charts")
	public List<Chart> getCharts() {
		return chartRepository.findAll();
	}

    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/charts/load", method = RequestMethod.POST, produces = "application/json")
    public void load(@RequestParam("file") final MultipartFile[] files) throws IOException {
        if (files.length != 1) {
            throw new IllegalArgumentException("exactly one file should be provided");
        }

        List<Chart> charts = (List<Chart>) objectMapper.readValue(files[0].getInputStream(),
            new TypeReference<List<Chart>>() {});
        chartRepository.deleteAll();
        chartRepository.save(charts);
    }

    @RequestMapping(value = "/charts/save", method = RequestMethod.GET, produces = "application/octet-stream")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String getChartsToSave() throws JsonProcessingException {
        return objectMapper.writeValueAsString(chartRepository.findAll());
    }

    @RequestMapping(value = "/charts", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
    @CacheEvict(value = "charts", allEntries = true)
	public void createChart(@RequestBody @Valid Chart chart) {
		chartRepository.save(chart);
	}

    @RequestMapping(value = "/charts/{chartId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    @CacheEvict(value = "charts", allEntries = true)
    public void removeChart(@PathVariable("chartId") String chartId) {
        chartRepository.delete(chartId);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public void handleIllegalArgumentException(IllegalArgumentException ex, HttpServletResponse response) {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
    }

}
