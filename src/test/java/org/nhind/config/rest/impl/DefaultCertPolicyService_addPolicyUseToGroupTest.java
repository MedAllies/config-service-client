package org.nhind.config.rest.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
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
import org.nhindirect.config.model.CertPolicy;
import org.nhindirect.config.model.CertPolicyGroup;
import org.nhindirect.config.model.CertPolicyGroupUse;
import org.nhindirect.config.model.CertPolicyUse;
import org.nhindirect.config.store.dao.CertPolicyDao;
import org.nhindirect.policy.PolicyLexicon;

public class DefaultCertPolicyService_addPolicyUseToGroupTest extends SpringBaseTest
{
	abstract class TestPlan extends BaseTestPlan 
	{
		protected Collection<CertPolicyGroup> groups;
		
		protected Collection<CertPolicy> policies;
		
		
		@Override
		protected void tearDownMocks()
		{

		}
		
		protected Collection<CertPolicyGroup> getGroupsToAdd()
		{
			try
			{
				groups = new ArrayList<CertPolicyGroup>();
				
				CertPolicyGroup group = new CertPolicyGroup();
				group.setPolicyGroupName("Group1");
				groups.add(group);
				
				group = new CertPolicyGroup();
				group.setPolicyGroupName("Group2");
				groups.add(group);
				
				return groups;
			}
			catch (Exception e)
			{
				throw new RuntimeException (e);
			}
		}
		
		protected Collection<CertPolicy> getPoliciesToAdd()
		{
			try
			{
				policies = new ArrayList<CertPolicy>();
				
				CertPolicy policy = new CertPolicy();
				policy.setPolicyName("Policy1");
				policy.setPolicyData(new byte[] {1,2,3});
				policy.setLexicon(PolicyLexicon.SIMPLE_TEXT_V1);
				policies.add(policy);
				
				policy = new CertPolicy();
				policy.setPolicyName("Policy2");
				policy.setPolicyData(new byte[] {1,2,5,6});
				policy.setLexicon(PolicyLexicon.JAVA_SER);
				policies.add(policy);
				
				return policies;
			}
			catch (Exception e)
			{
				throw new RuntimeException (e);
			}
		}
		
		protected abstract String getGroupNameToAssociate();
		
		protected abstract CertPolicyGroupUse getPolicyUseToAssociate();
		
		@Override
		protected void performInner() throws Exception
		{				
			
			final Collection<CertPolicy> policiesToAdd = getPoliciesToAdd();
			
			if (policiesToAdd != null)
			{
				for (CertPolicy addPolicy : policiesToAdd)
				{
					try
					{
						certPolService.addPolicy(addPolicy);
					}
					catch (ServiceException e)
					{
						throw e;
					}
				}
			}
			
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
			
			certPolService.addPolicyUseToGroup(getGroupNameToAssociate(), getPolicyUseToAssociate());
			
			final CertPolicyGroup getGroup = certPolService.getPolicyGroup(getGroupNameToAssociate());
			doAssertions(getGroup);
			
			
		}
			
		protected void doAssertions(CertPolicyGroup group) throws Exception
		{
			
		}
	}	
	
	@Test
	public void testAddPolicyUseToGroup_assertPolicyAdded()  throws Exception
	{
		new TestPlan()
		{

			@Override
			protected String getGroupNameToAssociate()
			{
				return "Group1";
			}
			
			@Override
			protected CertPolicyGroupUse getPolicyUseToAssociate()
			{
				final CertPolicyGroupUse use = new CertPolicyGroupUse();
				
				use.setIncoming(true);
				use.setOutgoing(true);
				use.setPolicyUse(CertPolicyUse.TRUST);
				use.setPolicy(policies.iterator().next());
				
				return use;
			}
			
			@Override
			protected void doAssertions(CertPolicyGroup group) throws Exception
			{
				
				assertNotNull(group);
				
				assertEquals(getGroupNameToAssociate(), group.getPolicyGroupName());
				assertEquals(1, group.getPolicies().size());
				
				final CertPolicyGroupUse use = group.getPolicies().iterator().next();
				assertEquals(policies.iterator().next().getPolicyName(), use.getPolicy().getPolicyName());
				assertEquals(CertPolicyUse.TRUST, use.getPolicyUse());
				assertTrue(use.isIncoming());
				assertTrue(use.isOutgoing());
				
			}
		}.perform();
	}		
	
	@Test
	public void testAddPolicyUseToGroup_nonExistantGroup_assertNotFound()  throws Exception
	{
		new TestPlan()
		{

			@Override
			protected String getGroupNameToAssociate()
			{
				return "Group4";
			}
			
			@Override
			protected CertPolicyGroupUse getPolicyUseToAssociate()
			{
				final CertPolicyGroupUse use = new CertPolicyGroupUse();
				
				use.setIncoming(true);
				use.setOutgoing(true);
				use.setPolicyUse(CertPolicyUse.TRUST);
				use.setPolicy(policies.iterator().next());
				
				return use;
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
	public void testAddPolicyUseToGroup_nonExistantPolicy_assertNotFound()  throws Exception
	{
		new TestPlan()
		{

			@Override
			protected String getGroupNameToAssociate()
			{
				return "Group1";
			}
			
			@Override
			protected CertPolicyGroupUse getPolicyUseToAssociate()
			{
				final CertPolicyGroupUse use = new CertPolicyGroupUse();
				
				final CertPolicy policy = new CertPolicy();
				policy.setPolicyName("bogus");
				
				use.setIncoming(true);
				use.setOutgoing(true);
				use.setPolicyUse(CertPolicyUse.TRUST);
				use.setPolicy(policy);
				
				return use;
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
	public void testAddPolicyUseToGroup_errorInGroupLookup_assertServiceError()  throws Exception
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
			protected Collection<CertPolicyGroup> getGroupsToAdd()
			{
				return null;
			}
			
			@Override
			protected Collection<CertPolicy> getPoliciesToAdd()
			{
				return null;
			}
			
			@Override
			protected void tearDownMocks()
			{
				super.tearDownMocks();
				
				certPolResource.setCertPolicyDao(policyDao);
			}
			
			@Override
			protected String getGroupNameToAssociate()
			{
				return "Group1";
			}
			
			@Override
			protected CertPolicyGroupUse getPolicyUseToAssociate()
			{
				final CertPolicyGroupUse use = new CertPolicyGroupUse();
				
				final CertPolicy policy = new CertPolicy();
				policy.setPolicyName("bogus");
				
				use.setIncoming(true);
				use.setOutgoing(true);
				use.setPolicyUse(CertPolicyUse.TRUST);
				use.setPolicy(policy);
				
				return use;
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
	public void testAddPolicyUseToGroup_errorInPolicyLookup_assertServiceError()  throws Exception
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
					doThrow(new RuntimeException()).when(mockDAO).getPolicyByName((String)any());
					
					certPolResource.setCertPolicyDao(mockDAO);
				}
				catch (Throwable t)
				{
					throw new RuntimeException(t);
				}
			}
			
			@Override
			protected Collection<CertPolicyGroup> getGroupsToAdd()
			{
				return null;
			}
			
			@Override
			protected Collection<CertPolicy> getPoliciesToAdd()
			{
				return null;
			}
			
			@Override
			protected void tearDownMocks()
			{
				super.tearDownMocks();
				
				certPolResource.setCertPolicyDao(policyDao);
			}
			
			@Override
			protected String getGroupNameToAssociate()
			{
				return "Group1";
			}
			
			@Override
			protected CertPolicyGroupUse getPolicyUseToAssociate()
			{
				final CertPolicyGroupUse use = new CertPolicyGroupUse();
				
				final CertPolicy policy = new CertPolicy();
				policy.setPolicyName("bogus");
				
				use.setIncoming(true);
				use.setOutgoing(true);
				use.setPolicyUse(CertPolicyUse.TRUST);
				use.setPolicy(policy);
				
				return use;
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
	public void testAddPolicyUseToGroup_errorInAssociate_assertServiceError()  throws Exception
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
					when(mockDAO.getPolicyByName((String)any())).thenReturn(new org.nhindirect.config.store.CertPolicy());
					doThrow(new RuntimeException()).when(mockDAO).addPolicyUseToGroup(eq(0L), eq(0L), (org.nhindirect.config.store.CertPolicyUse)any(),
							eq(true), eq(true));
					
					certPolResource.setCertPolicyDao(mockDAO);
				}
				catch (Throwable t)
				{
					throw new RuntimeException(t);
				}
			}
			
			@Override
			protected Collection<CertPolicyGroup> getGroupsToAdd()
			{
				return null;
			}
			
			@Override
			protected Collection<CertPolicy> getPoliciesToAdd()
			{
				return null;
			}
			
			@Override
			protected void tearDownMocks()
			{
				super.tearDownMocks();
				
				certPolResource.setCertPolicyDao(policyDao);
			}
			
			@Override
			protected String getGroupNameToAssociate()
			{
				return "Group1";
			}
			
			@Override
			protected CertPolicyGroupUse getPolicyUseToAssociate()
			{
				final CertPolicyGroupUse use = new CertPolicyGroupUse();
				
				final CertPolicy policy = new CertPolicy();
				policy.setPolicyName("Policy1");
				
				use.setIncoming(true);
				use.setOutgoing(true);
				use.setPolicyUse(CertPolicyUse.TRUST);
				use.setPolicy(policy);
				
				return use;
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