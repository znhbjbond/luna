package org.woodship.luna.db;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.woodship.luna.HomeView;
import org.woodship.luna.core.person.OrgType;
import org.woodship.luna.core.person.Organization;
import org.woodship.luna.core.person.OrganizationView;
import org.woodship.luna.core.person.Person;
import org.woodship.luna.core.person.PersonService;
import org.woodship.luna.core.person.PersonView;
import org.woodship.luna.core.security.ApplicationView;
import org.woodship.luna.core.security.Resource;
import org.woodship.luna.core.security.ResourceService;
import org.woodship.luna.core.security.ResourceType;
import org.woodship.luna.core.security.Role;
import org.woodship.luna.core.security.RoleDataScope;
import org.woodship.luna.core.security.RoleView;
import org.woodship.luna.core.security.User;
import org.woodship.luna.core.security.UserService;
import org.woodship.luna.core.security.UserView;
import org.woodship.luna.demo.simpleview.Vendor;
import org.woodship.luna.demo.simpleview.VendorView;
import org.woodship.luna.demo.subtable.Element;
import org.woodship.luna.demo.subtable.ElementScope;
import org.woodship.luna.demo.subtable.Product;
import org.woodship.luna.demo.subtable.ProductView;
import org.woodship.luna.util.Utils;


@Component
public class InitData implements Serializable{
	private static final long serialVersionUID = 6644623096240101511L;

	@PersistenceContext
	private  EntityManager em;

	@Autowired
	private ResourceService resSer;
	@Autowired
	private UserService userSer;
	@Autowired
	private PersonService personSer;

	private User userAdmin;
	
	private Resource home;

	private Resource resBase;

	private Resource resPerson;

	private Resource resOrg;
	
	
	@Value("${luna.company.name}")
	private String lunaCompanyName;
	
	
	
	@Transactional
	public void init(){
		//有数据则不再初始化
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		cq.select(cb.count(cq.from(User.class)));
		long size = em.createQuery(cq).getSingleResult();
		if(size>0) return;

		//增加人员机构用户数据
		createOrgAndPerson();

		//增加资源
		createResource();

		//增加业务数据
		createBusinessData();
	}


	private void createBusinessData() {

		//菜单
		Resource resBus = new Resource("BUSI_MODULE", "示例程序", ResourceType.MODULE);
		em.persist(resBus);
		Resource productView =resSer.createCUDApp("主子表", resBus,ProductView.class.getSimpleName(), ProductView.class);
		resSer.createCUDApp("简单视图", resBus,Vendor.class.getSimpleName().toLowerCase(), VendorView.class);
		
		//产品测试数据
		Product p2 = new Product("叶轮");
		p2.addElementScope(new ElementScope(Element.C,   0.0,  0.06 ));
		p2.addElementScope(new ElementScope(Element.Si,  0.0,   1 ));
		p2.addElementScope(new ElementScope(Element.Mn,0.0,   1));
		p2.addElementScope(new ElementScope(Element.P,   0.0,   0.035));
		p2.addElementScope(new ElementScope(Element.S,   0.0d,   0.0d));
		p2.addElementScope(new ElementScope(Element.Cr,  0.0d,   0.0d));
		p2.addElementScope(new ElementScope(Element.Ni,  4.5,   5.6));
		p2.addElementScope(new ElementScope(Element.Mo,   0.05,  0.15));
		p2.addElementScope(new ElementScope(Element.V,   0.0d,   0.0d));
		p2.addElementScope(new ElementScope(Element.W,  0.0d,   0.0d));
		p2.addElementScope(new ElementScope(Element.Cu,  0.0d,   0.0d));
		
		for(ElementScope es : p2.getEss()){
			em.persist(es);
		}
		em.persist(p2);
		
		Product p = new Product("衬板");
		p.addElementScope(new ElementScope(Element.C,   0.0,  0.06 ));
		p.addElementScope(new ElementScope(Element.Si,  0.0,   1 ));
		p.addElementScope(new ElementScope(Element.Mn,0.0,   1));
		p.addElementScope(new ElementScope(Element.P,   0.0,   0.035));
		p.addElementScope(new ElementScope(Element.S,   0.0,   0.035));
		p.addElementScope(new ElementScope(Element.Cr,  15.5, 17.5));
		p.addElementScope(new ElementScope(Element.Ni,  4.5,   5.6));
		p.addElementScope(new ElementScope(Element.Mo,   0.05,  0.15));
		p.addElementScope(new ElementScope(Element.V,   0.0d,   0.0d));
		p.addElementScope(new ElementScope(Element.W,  0.0d,   0.0d));
		p.addElementScope(new ElementScope(Element.Cu,  0.0d,   0.0d));
		
		for(ElementScope es : p.getEss()){
			em.persist(es);
		}
		em.persist(p);
		
		
		String password  = Utils.DEFAULT_PASSWORD;
		
		//增加一个配料管理员用户
		User userPLadmin = new User();
		userPLadmin.setPassword(password);
		userPLadmin.setUsername("user");
		userPLadmin.setShowName("用户1");
		em.persist(userPLadmin);
		
		//配料管理员
		Role rolePeiLiao = new Role("配料管理员");
		rolePeiLiao.setDataScope(RoleDataScope.全部数据);
		rolePeiLiao.addResource(home);
		rolePeiLiao.addResource(resBus);
		
		rolePeiLiao.addResource(productView);
		rolePeiLiao.addResource(resSer.getResByKey(Utils.getAddActionKey(ProductView.class)));
		rolePeiLiao.addResource(resSer.getResByKey(Utils.getEditActionKey(ProductView.class)));
		rolePeiLiao.addResource(resSer.getResByKey(Utils.getDelActionKey(ProductView.class)));
		
		rolePeiLiao.addUser(userPLadmin);
		
		userPLadmin.addRole(rolePeiLiao);
		em.persist(rolePeiLiao);
		
		
	}


	final static String[] groupsNames = { "甲班","乙班", "丙班" };
	final static String[] officeNames = { "办公室","炉子", "混沙"};
	final static String[] fnames = { "赵", "钱", "孙", "李",
		"周","吴","郑","王","冯","陈","褚",
		"卫","蒋","沈"};
	final static String[] lnames = { "万全", "心社", "彭勇", "建国",
		"定之", "洁敏", "正", "长赋", "焕成", "伏瞻",
		"卫", "继伟", "振华", "益民", "名照" };
	final static String cities[] = { "北京", "上海", "深圳",
		"广州", "杭州", "南京", "沈阳", "成都", "哈尔滨",
		"大连", "西安", "郑州", "洛阳" };
	final static String streets[] = { "4215 Blandit Av.", "452-8121 Sem Ave",
		"279-4475 Tellus Road", "4062 Libero. Av.", "7081 Pede. Ave",
		"6800 Aliquet St.", "P.O. Box 298, 9401 Mauris St.",
		"161-7279 Augue Ave", "P.O. Box 496, 1390 Sagittis. Rd.",
		"448-8295 Mi Avenue", "6419 Non Av.", "659-2538 Elementum Street",
		"2205 Quis St.", "252-5213 Tincidunt St.",
		"P.O. Box 175, 4049 Adipiscing Rd.", "3217 Nam Ave",
		"P.O. Box 859, 7661 Auctor St.", "2873 Nonummy Av.",
		"7342 Mi, Avenue", "539-3914 Dignissim. Rd.",
		"539-3675 Magna Avenue", "Ap #357-5640 Pharetra Avenue",
		"416-2983 Posuere Rd.", "141-1287 Adipiscing Avenue",
		"Ap #781-3145 Gravida St.", "6897 Suscipit Rd.",
		"8336 Purus Avenue", "2603 Bibendum. Av.", "2870 Vestibulum St.",
		"Ap #722 Aenean Avenue", "446-968 Augue Ave",
		"1141 Ultricies Street", "Ap #992-5769 Nunc Street",
		"6690 Porttitor Avenue", "Ap #105-1700 Risus Street",
		"P.O. Box 532, 3225 Lacus. Avenue", "736 Metus Street",
		"414-1417 Fringilla Street", "Ap #183-928 Scelerisque Road",
	"561-9262 Iaculis Avenue" };

	public  void createOrgAndPerson() {

		Random r = new Random(0);
		Organization orgRoot = new Organization();
		orgRoot.setName(StringUtils.isEmpty(lunaCompanyName)?"WoodShip":lunaCompanyName);
		orgRoot.setOrgType(OrgType.单位);
		//TODO 根单位没办法调用setParent方法
		em.persist(orgRoot);
		orgRoot.getAncestors().add(orgRoot);
		
		for (String o : officeNames) {
			Organization geoGroup = new Organization(orgRoot);
			geoGroup.setName(o);
			geoGroup.setOrgType(OrgType.顶级部门);
			em.persist(geoGroup);
			for (String g : groupsNames) {
				Organization group = new Organization(geoGroup);
				group.setName(g);
				group.setOrgType(OrgType.班组);
				em.persist(group);
				Set<Person> gPersons = new HashSet<Person>();

				int amount = r.nextInt(15) + 1;
				for (int i = 0; i < amount; i++) {
					Person p = new Person();
					p.setTrueName(fnames[r.nextInt(fnames.length)]+lnames[r.nextInt(lnames.length)]);
					p.setIdCard(cities[r.nextInt(cities.length)]);
					p.setPhoneNumber("+358 02 555 " + r.nextInt(10) + r.nextInt(10)
							+ r.nextInt(10) + r.nextInt(10));
					int n = r.nextInt(100000);
					if (n < 10000) {
						n += 10000;
					}
					p.setWorkNum("" + n);
					p.setStreet(streets[r.nextInt(streets.length)]);
					p.setOrg(group);
					gPersons.add(p);
					em.persist(p);

				}
				group.setPersons(gPersons);
			}
			
		}

	}



	private void createResource(){
		home = new Resource(HomeView.KEY,"主页", ResourceType.APPLICATION, null,  HomeView.NAME, HomeView.class);
		em.persist(home);

		//增加系统管理模块
		Resource sys = new Resource("SYSTEM_MODULE", "系统管理", ResourceType.MODULE);
		em.persist(sys);
		resSer.createApp("应用管理",  sys, ApplicationView.NAME, ApplicationView.class);
		resSer.createCUDApp("用户管理",  sys, UserView.NAME, UserView.class);
		resSer.createCUDApp("角色管理",  sys, RoleView.NAME, RoleView.class);

		//增加基础应用模块
		resBase = new Resource("BASE_MODULE", "基础应用", ResourceType.MODULE);
		em.persist(resBase);
		resOrg = resSer.createCUDApp("机构管理", resBase,OrganizationView.NAME, OrganizationView.class);
		resPerson = resSer.createCUDApp("人员管理", resBase,PersonView.NAME, PersonView.class);


		//增加管理员用户
		String pw =  Utils.DEFAULT_PASSWORD;
		userAdmin = new User(User.SUPER_ADMIN_USERNAME,pw,"管理员");
		userAdmin.setSysUser(true);
		em.persist(userAdmin);


		//管理员角色
		Role radmin = new Role(Role.SUPER_ADMIN_ROLE_NAME);
		radmin.setSysRole(true);
		radmin.addUser(userAdmin);
		userAdmin.addRole(radmin);
		radmin.setDataScope(RoleDataScope.全部数据);
		em.persist(radmin);

		//增加一个无任何权限的用户(用于测试)
		User uno = new User("no", pw, "无权限");
		em.persist(uno);
	}



}
