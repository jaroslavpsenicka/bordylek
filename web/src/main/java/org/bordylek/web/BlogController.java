package org.bordylek.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping(value = "/blog")
public class BlogController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${blog.url}")
    private String blogUrl;

    @Value("${blog.id}")
    private String blogId;

    @Value("${blog.key}")
    private String blogKey;

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @Cacheable("blog")
    public String findBlog() {
        String url = blogUrl + "/blogs/" + blogId + "?key=" + blogKey;
        return new String(restTemplate.getForObject(url, byte[].class));
    }

    @ResponseBody
    @RequestMapping(value = "/posts", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @Cacheable("blog")
    public String findPosts() {
        String url = blogUrl + "/blogs/" + blogId + "/posts?key=" + blogKey;
        return new String(restTemplate.getForObject(url, byte[].class));
    }

}
