package com.epam.moovies.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.epam.moovies.converters.LocalDateTimeConverter;
import com.epam.moovies.enums.Rating;
import com.epam.moovies.model.Auditorium;
import com.epam.moovies.model.Event;
import com.epam.moovies.service.AuditoriumService;
import com.epam.moovies.service.EventService;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

@Controller
@RequestMapping("events")
public class EventsController {

	@Autowired
	private AuditoriumService auditoryService;

	@Autowired
	private EventService eventService;

	@RequestMapping("all")
	public ModelAndView openAllEvents() {
		ModelAndView mav = new ModelAndView("events/allEvents");
		mav.addObject("events", eventService.getAll());
		return mav;
	}

	@RequestMapping(value = "/loadFromFile", method = RequestMethod.POST)
	public String loadEventsFromXml(@RequestParam("fileWithEvents") MultipartFile file) throws IOException {
		XStream xStream = new XStream(new StaxDriver());
		@SuppressWarnings("unchecked")
		List<Event> events = (List<Event>) xStream.fromXML(file.getInputStream());
		for (Event event : events) {
			eventService.create(event);
		}
		return "redirect:events/all";
	}

	@RequestMapping(value = "all/get", method = RequestMethod.GET)
	public void getEventsInXml(HttpServletResponse response) throws IOException {
		List<Event> all = eventService.getAll();
		XStream xStream = new XStream(new StaxDriver());
		response.setHeader("content-type", "application/xml");
		String xml = xStream.toXML(all);
		response.getWriter().write(xml);

	}

	@RequestMapping(path = "add", method = RequestMethod.POST)
	public String addEvent(@RequestParam Map<String, String> allRequestParams) {
		Event newEvent = new Event();
		// get auditorium id from request
		String auditoryIdString = allRequestParams.get("auditorium");
		Long auditoryId = Long.parseLong(auditoryIdString);
		Auditorium auditorium = auditoryService.getById(auditoryId);
		newEvent.setAuditorium(auditorium);
		// get name
		String name = allRequestParams.get("name");
		newEvent.setName(name);
		// get base price
		String basePriceString = allRequestParams.get("basePrice");
		long basePrice = Long.parseLong(basePriceString);
		newEvent.setBasePrice(basePrice);

		// get start
		String start = allRequestParams.get("start");
		LocalDateTime startDateTime = LocalDateTimeConverter.convert(start);
		newEvent.setStart(startDateTime);

		// get stop
		String end = allRequestParams.get("end");
		LocalDateTime endDateTime = LocalDateTimeConverter.convert(end);
		newEvent.setEnd(endDateTime);

		String ratingString = allRequestParams.get("rating");
		Rating rating = Rating.valueOf(ratingString);
		newEvent.setRating(rating);

		Event createdEvent = eventService.create(newEvent);
		return "redirect:/event/" + createdEvent.getId();
	}

	@RequestMapping("book/{eventId}")
	public ModelAndView openBookTicketForEventPage(@PathVariable("eventId") Long eventId) {
		ModelAndView openBookTicketForEventModelAndView = new ModelAndView("events/book");
		Event event = eventService.getById(eventId);
		openBookTicketForEventModelAndView.addObject("event", event);
		return openBookTicketForEventModelAndView;
	}

}
