package com.axelor.rpc;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;

import com.axelor.BaseTest;
import com.axelor.db.JPA;
import com.axelor.test.db.Address;
import com.axelor.test.db.Contact;
import com.axelor.test.db.Group;
import com.axelor.test.db.Title;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.persist.Transactional;

public class ResourceTest extends BaseTest {

	@Inject
	Resource<Contact> resource;
	
	@Test
	public void testFields() throws Exception {

		Response res = resource.fields();
		
		Assert.assertNotNull(res);
		Assert.assertNotNull(res.getData());
		Assert.assertTrue(res.getData() instanceof Map);
	}

	@Test
	public void testSearch() throws Exception {

		Request req = fromJson("find3.js", Request.class);
		Response res = resource.search(req);
		
		Assert.assertNotNull(res);
		Assert.assertNotNull(res.getData());
		Assert.assertTrue(res.getData() instanceof List);
	}

	@Test @SuppressWarnings("all")
	@Transactional
	public void testAdd() throws Exception {

		Request req = fromJson("add2.js", Request.class);
		Response res = resource.save(req);
		
		Assert.assertNotNull(res);
		Assert.assertNotNull(res.getData());
		Assert.assertNotNull(res.getItem(0));
		Assert.assertTrue(res.getItem(0) instanceof Contact);

		Contact p = (Contact) res.getItem(0);

		Assert.assertEquals(Title.class, p.getTitle().getClass());
		Assert.assertEquals(Address.class, p.getAddresses().get(0).getClass());
		Assert.assertEquals(Group.class, p.getGroup(0).getClass());
		Assert.assertEquals(LocalDate.class, p.getDateOfBirth().getClass());

		Assert.assertEquals("mr", p.getTitle().getCode());
		Assert.assertEquals("France", p.getAddresses().get(0).getCountry().getName());
		Assert.assertEquals("family", p.getGroup(0).getName());
		Assert.assertEquals("1977-05-01", p.getDateOfBirth().toString());
	}
	
	@Test @SuppressWarnings("all")
	@Transactional
	public void testUpdate() throws Exception {

		Contact c = Contact.all().fetchOne();
		Map<String, Object> data = Maps.newHashMap();
		
		data.put("id", c.getId());
		data.put("version", c.getVersion());
		data.put("firstName", "jack");
		data.put("lastName", "sparrow");

		String json = toJson(ImmutableMap.of("data", data));

		Request req = fromJson(json, Request.class);
		Response res = resource.save(req);
		
		Assert.assertNotNull(res);
		Assert.assertNotNull(res.getData());
		Assert.assertNotNull(res.getItem(0));
		Assert.assertTrue(res.getItem(0) instanceof Contact);

		Contact contact = (Contact) res.getItem(0);
		
		Assert.assertEquals("jack", contact.getFirstName());
		Assert.assertEquals("sparrow", contact.getLastName());
	}
	

	@Test
	public void testCopy() {
		
		Contact c = Contact.all().filter("firstName = ?", "James").fetchOne();
		Contact n = JPA.copy(c, true);
		
		Assert.assertNotSame(c, n);
		Assert.assertNotSame(c.getAddresses(), n.getAddresses());
		Assert.assertEquals(c.getAddresses().size(),
							n.getAddresses().size());
		
		Assert.assertSame(c, c.getAddresses().get(0).getContact());
		Assert.assertSame(n, n.getAddresses().get(0).getContact());
		Assert.assertNotSame(c.getAddresses().get(0).getContact(), 
							 n.getAddresses().get(0).getContact());
	}
}
