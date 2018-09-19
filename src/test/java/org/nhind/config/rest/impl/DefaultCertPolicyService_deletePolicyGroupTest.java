package org.nhind.config.rest.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.nhind.config.client.SpringBaseTest;
import org.nhind.config.testbase.BaseTestPlan;

import org.nhindirect.common.rest.exceptions.ServiceException;
import org.nhindirect.common.rest.exceptions.ServiceMethodException;

import org.nhindirect.config.model.CertPolicyGroup;
import org.nhindirect.config.store.dao.CertPolicyDao;

public class DefaultCertPolicyService_deletePolicyGroupTest extends SpringBaseTest
{
	abstract class TestPlan extends BaseTestPlan 
	{
		@Override
		protected void tearDownMocks()
		{

		}
	
	protected abstract Collection<CertPolicyGroup> getGroupsToAdd();
	
	protected abstract String getPolicyGroupToDelete();
	
	@Override
	protected void performInner() throws Exception
	{				
		
		final Collection<CertPolicyGroup> groupsToAdd = getGroupsToAdd();
		
		if (groupsToAdd != null)
		{
			for (CertPolicyGroup addGroup : groupsToAdd)
			{
				try
				{
					certPolService.addPolicyGroup(addGroup);
				}
				catch (ServiceException e)
				{
					throw e;
				}
			}
		}
		
		certPolService.deletePolicyGroup(getPolicyGroupToDelete());
		
		doAssertions();
		
	}
		
	protected void doAssertions() throws Exception
	{
		
	}
}	

@Test
public void testRemovePolicyGroupByName_assertGroupRemoved()  throws Exception
{
	new TestPlan()
	{
		protected Collection<CertPolicyGroup> groups;
		
		@Override
		protected Collection<CertPolicyGroup> getGroupsToAdd()
		{
			try
			{
				groups = new ArrayList<CertPolicyGroup>();
				
				CertPolicyGroup group = new CertPolicyGroup();
				group.setPolicyGroupName("Group1");
				groups.add(group);
				
				return groups;
			}
			catch (Exception e)
			{
				throw new RuntimeException (e);
			}
		}

		@Override
		protected String getPolicyGroupToDelete()
		{
			return "Group1";
		}
		
		@Override
		protected void doAssertions() throws Exception
		{
			assertNull(policyDao.getPolicyGroupByName(getPolicyGroupToDelete()));
		}
	}.perform();
}


@Test
public void testRemovePolicyGroupByName_nonExistantGroup_assertNotFound()  throws Exception
{
	new TestPlan()
	{
		protected Collection<CertPolicyGroup> groups;
		
		@Override
		protected Collection<CertPolicyGroup> getGroupsToAdd()
		{
			try
			{
				groups = new ArrayList<CertPolicyGroup>();
				
				CertPolicyGroup group = new CertPolicyGroup();
				group.setPolicyGroupName("Group1");
				groups.add(group);
				
				return groups;
			}
			catch (Exception e)
			{
				throw new RuntimeException (e);
			}
		}

		@Override
		protected String getPolicyGroupToDelete()
		{
			return "Group2";
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
public void testRemovePolicyGroupByName_errorInLookup_assertServiceError()  throws Exception
{
	new TestPlan()
	{
		@Override
		protected void setupMocks()
		{
			try
			{
				super.setupMocks();
				CertPolicyDao mockDAO = mock(CertPolicyDao.class);
				doThrow(new RuntimeException()).when(mockDAO).getPolicyGroupByName((String)any());
				
				certPolResource.setCertPolicyDao(mockDAO);
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
			
			certPolResource.setCertPolicyDao(policyDao);
		}	
		
		@Override
		protected Collection<CertPolicyGroup> getGroupsToAdd()
		{
			return null;
		}
		
		@Override
		protected String getPolicyGroupToDelete()
		{
			return "Group1";
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
public void testRemovePolicyGroupByName_errorInDelete_assertServiceError()  throws Exception
{
	new TestPlan()
	{
		@Override
		protected void setupMocks()
		{
			try
			{
				super.setupMocks();
				CertPolicyDao mockDAO = mock(CertPolicyDao.class);
				when(mockDAO.getPolicyGroupByName((String)any())).thenReturn(new org.nhindirect.config.store.CertPolicyGroup());
				doThrow(new RuntimeException()).when(mockDAO).deletePolicyGroups((long[])any());
				
				certPolResource.setCertPolicyDao(mockDAO);
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
			
			certPolResource.setCertPolicyDao(policyDao);
		}	
		
		@Override
		protected Collection<CertPolicyGroup> getGroupsToAdd()
		{
			return null;
		}
		
		@Override
		protected String getPolicyGroupToDelete()
		{
			return "Group1";
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


