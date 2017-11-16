package de.moonflower.jfritz.phonebookNew;

import junit.framework.Assert;

import static org.mockito.Mockito.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.moonflower.jfritz.phonebookNew.PhoneBookBase;
import de.moonflower.jfritz.phonebookNew.PhoneBookManager;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumberOld;

public class PhoneBookManagerTest {

	@Mock private PhoneBookBase mockedPhoneBook;
	@Mock private PhoneNumberOld mockedPhoneNumber;
	@Mock private Person mockedPerson;

	private PhoneBookManager pbm;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		pbm = new PhoneBookManager();
	}

	@Test
	public void testRegisterPhoneBook() {
		Assert.assertEquals(0, pbm.listOfPhoneBooks.size());
		pbm.registerPhonebook(mockedPhoneBook);
		Assert.assertEquals(1, pbm.listOfPhoneBooks.size());
	}

	@Test
	public void testUnregisterPhoneBook() {
		Assert.assertEquals(0, pbm.listOfPhoneBooks.size());
		pbm.listOfPhoneBooks.add(mockedPhoneBook);
		Assert.assertEquals(1, pbm.listOfPhoneBooks.size());
		pbm.unregisterPhonebook(mockedPhoneBook);
		Assert.assertEquals(0, pbm.listOfPhoneBooks.size());
	}

	@Test
	public void testFindFirstPersonNoPhoneBookRegistered() {
		pbm.findFirstPerson(mockedPhoneNumber);
		verifyNoMoreInteractions(mockedPhoneNumber);
		verifyNoMoreInteractions(mockedPhoneBook);
	}

	@Test
	public void testFindFirstPersonWithPhoneBookRegistered() {
		doReturn(mockedPerson).when(mockedPhoneBook).findFirstPerson(mockedPhoneNumber);
		doReturn(true).when(mockedPhoneBook).contains(mockedPhoneNumber);

		pbm.registerPhonebook(mockedPhoneBook);
		Person result = pbm.findFirstPerson(mockedPhoneNumber);

		Assert.assertEquals(mockedPerson, result);

		verify(mockedPhoneBook, times(1)).contains(mockedPhoneNumber);
		verify(mockedPhoneBook, times(1)).findFirstPerson(mockedPhoneNumber);

		verifyNoMoreInteractions(mockedPhoneNumber);
		verifyNoMoreInteractions(mockedPhoneBook);
	}
}
