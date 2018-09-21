package org.nhind.config.rest.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.nhind.config.client.SpringBaseTest;
import org.nhind.config.testbase.BaseTestPlan;

import org.nhindirect.common.rest.exceptions.ServiceException;
import org.nhindirect.common.rest.exceptions.ServiceMethodException;

import org.nhindirect.config.model.Address;

import org.nhindirect.config.store.dao.AddressDao;


public class DefaultAddressService_deleteAddressTest extends SpringBaseTest
{

		abstract class TestPlan extends BaseTestPlan 
		{
			
			@Override
			protected void tearDownMocks()
			{

			}
			
			protected abstract Address getAddressToAdd();
			
			protected abstract String getDomainToAdd();
			
			protected abstract String getAddressNameToRemove();
			
			@Override
			protected void performInner() throws Exception
			{				
				
				final Address addAddress = getAddressToAdd();
				final String domainName = getDomainToAdd();
				
				if (domainName != null && !domainName.isEmpty())
				{
					final org.nhindirect.config.store.Domain domain = new org.nhindirect.config.store.Domain();
					domain.setDomainName(domainName);
					domain.setStatus(org.nhindirect.config.store.EntityStatus.ENABLED);
					domainDao.add(domain);
					
					if (addAddress != null)
						addAddress.setDomainName(domainName);
				}
				
				if (addAddress != null)
				{
					try
					{
						addressService.addAddress(addAddress);
					}
					catch (ServiceException e)
					{
		
						throw e;
					}
				}
				
				try
				{
					addressService.deleteAddress(getAddressNameToRemove());
				}
				catch (ServiceException e)
				{
					throw e;
				}
				
				doAssertions();
			}
			
			
			protected void doAssertions() throws Exception
			{
				
			}
		}	
		
		@Test
		public void testRemoveAddress_removeExistingAddress_assertAddressRemoved() throws Exception
		{
			new TestPlan()
			{
				protected Address address;
				
				@Override
				protected  Address getAddressToAdd()
				{
					address = new Address();
					
					address.setEmailAddress("me@test.com");
					address.setType("email");
					address.setEndpoint("none");
					address.setDisplayName("me");
					
					return address;
				}
				
				@Override
				protected String getDomainToAdd()
				{
					return "test.com";
				}
				
				@Override
				protected String getAddressNameToRemove()
				{
					return "me@test.com";
				}
				
				@Override
				protected void doAssertions() throws Exception
				{
					assertNull(addressDao.get("me@test.com"));
				}
			}.perform();
		}	
		
		@Test
		public void testRemoveAddress_nonExistentAddress_assertNotFound() throws Exception
		{
			new TestPlan()
			{
				protected Address address;
				
				@Override
				protected  Address getAddressToAdd()
				{
					address = new Address();
					
					address.setEmailAddress("me@test.com");
					address.setType("email");
					address.setEndpoint("none");
					address.setDisplayName("me");
					
					return address;
				}
				
				@Override
				protected String getDomainToAdd()
				{
					return "test.com";
				}
				
				@Override
				protected String getAddressNameToRemove()
				{
					return "me@test2.com";
				}
				
				@Override
				protected void assertException(Exception exception) throws Exception 
				{
					assertTrue(exception instanceof ServiceMethodException);
					ServiceMethodException ex = (ServiceMethodException)exception;
					assertEquals(404, ex.getResponseCode());
				}
			}.perform();
		}		
		
		@Test
		public void testRemoveAddress_nonErrorInDelete_assertServerError() throws Exception
		{
			new TestPlan()
			{
				@Override
				protected void setupMocks()
				{
					try
					{
						super.setupMocks();

						AddressDao mockDAO = mock(AddressDao.class);
						when(mockDAO.get((String)any())).thenReturn(new org.nhindirect.config.store.Address());
						doThrow(new RuntimeException()).when(mockDAO).delete(eq("me@test.com"));
						
						addressResource.setAddressDao(mockDAO);
					}
					catch (Throwable t)
					{
						throw new RuntimeException(t);
					}
				}	
				
				@Override
				protected void tearDownMocks()
				{
					super.tearDownMocks();
					
					addressResource.setAddressDao(addressDao);
				}
				
				@Override
				protected  Address getAddressToAdd()
				{
					
					return null;
				}
				
				@Override
				protected String getDomainToAdd()
				{
					return "test.com";
				}
				
				@Override
				protected String getAddressNameToRemove()
				{
					return "me@test.com";
				}
				
				@Override
				protected void assertException(Exception exception) throws Exception 
				{
					assertTrue(exception instanceof ServiceMethodException);
					ServiceMethodException ex = (ServiceMethodException)exception;
					assertEquals(500, ex.getResponseCode());
				}
			}.perform();
		}	
		
		@Test
		public void testRemoveAddress_nonErrorInLookup_assertServerError() throws Exception
		{
			new TestPlan()
			{

				
				@Override
				protected void setupMocks()
				{
					try
					{
						super.setupMocks();
						AddressDao mockDAO = mock(AddressDao.class);
						doThrow(new RuntimeException()).when(mockDAO).get(eq("me@test.com"));
						
						addressResource.setAddressDao(mockDAO);
					}
					catch (Throwable t)
					{
						throw new RuntimeException(t);
					}
				}	
				
				@Override
				protected void tearDownMocks()
				{
					super.tearDownMocks();
					
					addressResource.setAddressDao(addressDao);
				}
				
				@Override
				protected  Address getAddressToAdd()
				{
					
					return null;
				}
				
				@Override
				protected String getDomainToAdd()
				{
					return "test.com";
				}
				
				@Override
				protected String getAddressNameToRemove()
				{
					return "me@test.com";
				}
				
				@Override
				protected void assertException(Exception exception) throws Exception 
				{
					assertTrue(exception instanceof ServiceMethodException);
					ServiceMethodException ex = (ServiceMethodException)exception;
					assertEquals(500, ex.getResponseCode());
				}
			}.perform();
		}			
	}