package de.moonflower.jfritz.testsuites.phonebook;

import java.util.List;
import java.util.Vector;

import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.phonebook.NumberPersonMultiHashMap;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;
import junit.framework.TestCase;

public class NumberMultiHashMapTest extends TestCase {

	public void testMultiHashMap()
	{
		new Main();
		Vector<Person> vector = new Vector<Person>();
		NumberPersonMultiHashMap map = new NumberPersonMultiHashMap();

		// Test insert one object
		PhoneNumber num1_1 = new PhoneNumber("1234", false);
		PhoneNumber num1_2 = new PhoneNumber("5678", false);
		PhoneNumber num1_3 = new PhoneNumber("9101", false);

		Person person = new Person();
		person.addNumber(num1_1);
		person.addNumber(num1_2);
		person.addNumber(num1_3);

		person.setCity("city");
		person.setCompany("company");
		person.setEmailAddress("email");
		person.setFirstName("fistName");
		person.setLastName("lastName");
		person.setPictureUrl("pictureUrl");
		person.setPostalCode("12345");
		person.setPrivateEntry(true);
		person.setStreet("street");
		person.setStandard("1254");

		map.addPerson(num1_1, person);
		map.addPerson(num1_2, person);
		vector.add(person);

		List<Person> result = map.getAllPerson();
		assertTrue(result.contains(person));

		result = map.getPerson(num1_1);
		assertTrue(result.contains(person));

		result = map.getPerson(num1_2);
		assertTrue(result.contains(person));

		result = map.getPerson(num1_3);
		assertTrue(result == null);

		person.setCity("cityNew");

		result = map.getPerson(num1_1);
		assertTrue(result.contains(person));

		assertTrue(vector.get(0).equals(person));

		// test insert multiple objects
		Person person2 = new Person();
		PhoneNumber num2 = new PhoneNumber("9876", false);
		person2.addNumber(num2);
		person2.setCity("city2");
		person2.setCompany("company2");
		person2.setEmailAddress("email2");
		person2.setFirstName("fistName2");
		person2.setLastName("lastName2");
		person2.setPictureUrl("pictureUrl2");
		person2.setPostalCode("123452");
		person2.setPrivateEntry(false);
		person2.setStreet("street2");
		person2.setStandard("12542");

		map.addPerson(num2, person2);
		vector.add(person2);

		Person person3 = new Person();
		PhoneNumber num3 = new PhoneNumber("3772", false);
		person3.addNumber(num3);
		person3.setCity("city3");
		person3.setCompany("company3");
		person3.setEmailAddress("email3");
		person3.setFirstName("fistName3");
		person3.setLastName("lastName3");
		person3.setPictureUrl("pictureUrl3");
		person3.setPostalCode("123453");
		person3.setPrivateEntry(false);
		person3.setStreet("street3");
		person3.setStandard("12543");

		map.addPerson(num3, person3);
		vector.add(person3);

		Person person4 = new Person();
		PhoneNumber num4 = new PhoneNumber("3772", false);
		person4.addNumber(num4);
		person4.setCity("city4");
		person4.setCompany("company4");
		person4.setEmailAddress("email4");
		person4.setFirstName("fistName4");
		person4.setLastName("lastName4");
		person4.setPictureUrl("pictureUrl4");
		person4.setPostalCode("123454");
		person4.setPrivateEntry(false);
		person4.setStreet("street4");
		person4.setStandard("12544");

		map.addPerson(num4, person4);
		vector.add(person4);


		result = map.getAllPerson();
		assertTrue(result.contains(person));
		assertTrue(result.contains(person2));
		assertTrue(result.contains(person3));
		assertTrue(result.contains(person4));

		result = map.getPerson(num1_1);
		assertTrue(result.contains(person));

		result = map.getPerson(num1_2);
		assertTrue(result.contains(person));

		result = map.getPerson(num2);
		assertTrue(result.contains(person2));

		result = map.getPerson(num3);
		assertTrue(result.contains(person3));

		result = map.getPerson(num4);
		assertTrue(result.contains(person4));

		// test delete one number of person with 2 numbers
		map.deletePerson(num1_1, person);
		result = map.getPerson(num1_1);
		assertTrue(result.size() == 0);
		result = map.getPerson(num1_2);
		assertTrue(result.contains(person));

		// test delete one number of a person with only one number
		map.deletePerson(num2, person2);
		result = map.getPerson(num2);
		assertTrue(result.size() == 0);

		// test delete one number of a person with only one number,
		// but this number also belongs to a second person
		map.deletePerson(num4, person4);
		result = map.getPerson(num4);
		assertTrue(result.size() == 1);
		// second person should remain in hashMap
		result = map.getPerson(num3);
		assertTrue(result.contains(person3));

		// delete also the last entry with number 3772
		map.deletePerson(num4, person3);
		result = map.getPerson(num4);
		assertTrue(result.size() == 0);
	}
}
