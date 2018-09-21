package org.nhind.config.client;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.nhind.config.rest.AddressService;
import org.nhind.config.rest.AnchorService;
import org.nhind.config.rest.CertPolicyService;
import org.nhind.config.rest.CertificateService;
import org.nhind.config.rest.DNSService;
import org.nhind.config.rest.DomainService;
import org.nhind.config.rest.SettingService;
import org.nhind.config.rest.TrustBundleService;
import org.nhindirect.config.resources.AddressResource;
import org.nhindirect.config.resources.AnchorResource;
import org.nhindirect.config.resources.CertPolicyResource;
import org.nhindirect.config.resources.CertificateResource;
import org.nhindirect.config.resources.DNSResource;
import org.nhindirect.config.resources.DomainResource;
import org.nhindirect.config.resources.SettingResource;
import org.nhindirect.config.resources.TrustBundleResource;
import org.nhindirect.config.store.Address;
import org.nhindirect.config.store.Anchor;
import org.nhindirect.config.store.CertPolicy;
import org.nhindirect.config.store.CertPolicyGroup;
import org.nhindirect.config.store.Certificate;
import org.nhindirect.config.store.DNSRecord;
import org.nhindirect.config.store.Domain;
import org.nhindirect.config.store.Setting;
import org.nhindirect.config.store.TrustBundle;
import org.nhindirect.config.store.dao.AddressDao;
import org.nhindirect.config.store.dao.AnchorDao;
import org.nhindirect.config.store.dao.CertPolicyDao;
import org.nhindirect.config.store.dao.CertificateDao;
import org.nhindirect.config.store.dao.DNSDao;
import org.nhindirect.config.store.dao.DomainDao;
import org.nhindirect.config.store.dao.SettingDao;
import org.nhindirect.config.store.dao.TrustBundleDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;
import org.xbill.DNS.Type;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class, webEnvironment = WebEnvironment.DEFINED_PORT)
public abstract class SpringBaseTest
{
	protected String filePrefix;
	
	@Autowired
	protected AddressService addressService;
	
	@Autowired 
	protected AddressResource addressResource;
	
	@Autowired
	protected AnchorService anchorService;
	
	@Autowired 
	protected AnchorResource anchorResource;	
	
	@Autowired
	protected CertificateService certService;
	
	@Autowired 
	protected CertificateResource certResource;		
	
	@Autowired
	protected CertPolicyService certPolService;
	
	@Autowired 
	protected CertPolicyResource certPolResource;		

	@Autowired
	protected DomainService domainService;
	
	@Autowired 
	protected DomainResource domainResource;	
	
	@Autowired
	protected DNSService dnsService;
	
	@Autowired
	protected DNSResource dnsResource;
	
	@Autowired
	protected SettingService settingService;
	
	@Autowired
	protected SettingResource settingResource;
	
	@Autowired
	protected TrustBundleService bundleService;
	
	@Autowired
	protected TrustBundleResource bundleResource;
	
	@Autowired
	protected AddressDao addressDao;
	
	@Autowired	
	protected TrustBundleDao trustDao;
	
	@Autowired
	protected DomainDao domainDao;
	
	@Autowired
	protected AnchorDao anchorDao;
	
	@Autowired
	protected CertificateDao certDao;
	
	@Autowired 
	protected DNSDao dnsDao;
	
	@Autowired
	protected SettingDao settingDao;
	
	@Autowired
	protected CertPolicyDao policyDao;
	
	@Autowired
	protected TrustBundleDao bundleDao;	
	
	@Before
	public void setUp()
	{
		
		// check for Windows... it doens't like file://<drive>... turns it into FTP
		File file = new File("./src/test/resources/bundles/signedbundle.p7b");
		if (file.getAbsolutePath().contains(":/"))
			filePrefix = "file:///";
		else
			filePrefix = "file:///";
		
		try
		{
			cleanDataStore();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		
		// clean up the file system
		File dir = new File("./target/tempFiles");
		if (dir.exists())
		try
		{
			FileUtils.cleanDirectory(dir);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	protected void cleanDataStore() throws Exception
	{		
		// clean anchors
		final List<Anchor> anchors = anchorDao.listAll();
		
		if (!anchors.isEmpty())
		{
			final List<Long> anchorIds = new ArrayList<Long>();
			for (Anchor anchor : anchors)
				anchorIds.add(anchor.getId());
				
			anchorDao.delete(anchorIds);
		}
		// clean domains and the trust bundle domain relationships
		final List<Domain> domains = domainDao.listDomains(null, domainDao.count());
		if (domains != null)
		{
			for (Domain domain : domains)
			{
				Collection<Address> addresses = addressDao.getByDomain(domain, null);
				if (addresses != null)
				{
					for (Address address : addresses)
					{
						addressDao.delete(address.getEmailAddress());
					}
				}
				
				trustDao.disassociateTrustBundlesFromDomain(domain.getId());
				domainDao.delete(domain.getId());

			}
		}
		assertEquals(0, domainDao.count());
		
		//clean trust bundles
		Collection<TrustBundle> bundles = trustDao.getTrustBundles();
		for (TrustBundle bundle : bundles)
			trustDao.deleteTrustBundles(new long[] {bundle.getId()});
		
		bundles = trustDao.getTrustBundles();
		assertEquals(0, bundles.size());
		
		// clean certificates
		final List<Certificate> certs = certDao.list((String)null);
		if (!certs.isEmpty())
		{
			for (Certificate cert : certs)
			{
				certDao.delete(cert.getOwner());
			}
		}
		
		// clean DNS records
		final Collection<DNSRecord> records = dnsDao.get(Type.ANY);
		if (!records.isEmpty())
		{
			for (DNSRecord record : records)
				dnsDao.remove(record.getId());
		}
		
		// clean settings
		final Collection<Setting> settings = settingDao.getAll();
		if (!settings.isEmpty())
		{
			for (Setting setting : settings)
				settingDao.delete(Arrays.asList(setting.getName()));
		}	
		
		// clean policies
		final Collection<CertPolicy> policies = policyDao.getPolicies();
		if (!policies.isEmpty())
		{
			for (CertPolicy policy : policies)
				policyDao.deletePolicies(new long[] {policy.getId()});
		}
		
		// clean policy groups
		final Collection<CertPolicyGroup> groups = policyDao.getPolicyGroups();
		if (!groups.isEmpty())
		{
			for (CertPolicyGroup group : groups)
				policyDao.deletePolicyGroups(new long[] {group.getId()});
		}		
	}
}