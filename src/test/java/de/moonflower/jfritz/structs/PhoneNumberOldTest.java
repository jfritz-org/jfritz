package de.moonflower.jfritz.structs;

import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.struct.PhoneNumberOld;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PhoneNumberOldTest {

    @Test
    public void testIsNumeric() {
        assertFalse(PhoneNumberOld.isNumeric(null));
        assertFalse(PhoneNumberOld.isNumeric(""));
        assertFalse(PhoneNumberOld.isNumeric(" "));
        assertFalse(PhoneNumberOld.isNumeric("a"));
        assertFalse(PhoneNumberOld.isNumeric("+"));
        assertFalse(PhoneNumberOld.isNumeric("+123"));
        assertFalse(PhoneNumberOld.isNumeric("-123"));
        assertFalse(PhoneNumberOld.isNumeric(" 123"));
        assertFalse(PhoneNumberOld.isNumeric("123 "));
        assertFalse(PhoneNumberOld.isNumeric("**798"));

        assertTrue(PhoneNumberOld.isNumeric("0"));
        assertTrue(PhoneNumberOld.isNumeric("123"));
    }

    @Test
    public void testIsNumericOrStartsWithPlusSign() {
        assertFalse(PhoneNumberOld.isNumericOrNumericWithPlusSign(null));
        assertFalse(PhoneNumberOld.isNumericOrNumericWithPlusSign(""));
        assertFalse(PhoneNumberOld.isNumericOrNumericWithPlusSign(" "));
        assertFalse(PhoneNumberOld.isNumericOrNumericWithPlusSign("a"));
        assertFalse(PhoneNumberOld.isNumericOrNumericWithPlusSign("+"));
        assertFalse(PhoneNumberOld.isNumericOrNumericWithPlusSign("-123"));
        assertFalse(PhoneNumberOld.isNumericOrNumericWithPlusSign(" 123"));
        assertFalse(PhoneNumberOld.isNumericOrNumericWithPlusSign("123 "));
        assertFalse(PhoneNumberOld.isNumericOrNumericWithPlusSign("**798"));

        assertTrue(PhoneNumberOld.isNumericOrNumericWithPlusSign("0"));
        assertTrue(PhoneNumberOld.isNumericOrNumericWithPlusSign("123"));
        assertTrue(PhoneNumberOld.isNumericOrNumericWithPlusSign("+123"));
        assertTrue(PhoneNumberOld.isNumericOrNumericWithPlusSign("+4972112345678"));
    }

    @Test
    public void testIsValidForReverseLookup() {
        PhoneNumberOld n;

        n = new PhoneNumberOld(PropertyProvider.getInstance(), null, false);
        assertFalse(n.isValidForReverseLookup());

        n = new PhoneNumberOld(PropertyProvider.getInstance(), "", false);
        assertFalse(n.isValidForReverseLookup());

        n = new PhoneNumberOld(PropertyProvider.getInstance(), " ", false);
        assertFalse(n.isValidForReverseLookup());

        n = new PhoneNumberOld(PropertyProvider.getInstance(), "a", false);
        assertFalse(n.isValidForReverseLookup());

        n = new PhoneNumberOld(PropertyProvider.getInstance(), "**798", false);
        assertFalse(n.isValidForReverseLookup());

        n = new PhoneNumberOld(PropertyProvider.getInstance(), "1234567@sipgate.de", false);
        assertFalse(n.isValidForReverseLookup());

        n = new PhoneNumberOld(PropertyProvider.getInstance(), " 1234567", false); // space will be deleted
        assertTrue(n.isValidForReverseLookup());

        n = new PhoneNumberOld(PropertyProvider.getInstance(), "1234567 ", false); // space will be deleted
        assertTrue(n.isValidForReverseLookup());

        n = new PhoneNumberOld(PropertyProvider.getInstance(), "1234567", false);
        assertTrue(n.isValidForReverseLookup());

        n = new PhoneNumberOld(PropertyProvider.getInstance(), "+1234567", false);
        assertTrue(n.isValidForReverseLookup());

        n = new PhoneNumberOld(PropertyProvider.getInstance(), "+497211234567", false);
        assertTrue(n.isValidForReverseLookup());
    }

    @Test
    public void testFboxTelephoneCodes() {
        assertFalse(PhoneNumberOld.isFbTcode("+491791234567"));
        assertFalse(PhoneNumberOld.isFbTcode("00491791234567"));
        assertFalse(PhoneNumberOld.isFbTcode("01791234567"));
        assertFalse(PhoneNumberOld.isFbTcode("1234567"));
        assertFalse(PhoneNumberOld.isFbTcode("123"));

        // an Liste von http://www.wehavemorefun.de/fritzbox/Tastencodes orientiert
        assertTrue(PhoneNumberOld.isFbTcode("**09")); //  Heranholen eines Anrufs

        assertTrue(PhoneNumberOld.isFbTcode("*10#")); //  Über das Analog-Festnetz (POTS) die folgende Nummer wählen
        assertTrue(PhoneNumberOld.isFbTcode("*10#1234567")); //  Über das Analog-Festnetz (POTS) die Nummer 1234567 wählen

        assertTrue(PhoneNumberOld.isFbTcode("*11#")); //  Über das ISDN-Festnetz die folgende Nummer wählen
        assertTrue(PhoneNumberOld.isFbTcode("*11#1234567")); //  Über das ISDN-Festnetz die Nummer 1234567 wählen

        assertTrue(PhoneNumberOld.isFbTcode("*110#")); //  Über das ISDN-Festnetz mit der 10. MSN die folgende Nummer wählen
        assertTrue(PhoneNumberOld.isFbTcode("*110#1234567")); //  Über das ISDN-Festnetz mit der 10. MSN die Nummer 1234567 wählen
        assertTrue(PhoneNumberOld.isFbTcode("*111#")); //  Über das ISDN-Festnetz mit der 1. MSN die folgende Nummer wählen
        assertTrue(PhoneNumberOld.isFbTcode("*111#1234567")); //  Über das ISDN-Festnetz mit der 1. MSN die Nummer 1234567 wählen
        assertTrue(PhoneNumberOld.isFbTcode("*112#")); //  Über das ISDN-Festnetz mit der 2. MSN die folgende Nummer wählen
        assertTrue(PhoneNumberOld.isFbTcode("*112#1234567")); //  Über das ISDN-Festnetz mit der 2. MSN die Nummer 1234567 wählen
        assertTrue(PhoneNumberOld.isFbTcode("*113#")); //  Über das ISDN-Festnetz mit der 3. MSN die folgende Nummer wählen
        assertTrue(PhoneNumberOld.isFbTcode("*113#1234567")); //  Über das ISDN-Festnetz mit der 3. MSN die Nummer 1234567 wählen
        assertTrue(PhoneNumberOld.isFbTcode("*114#")); //  Über das ISDN-Festnetz mit der 4. MSN die folgende Nummer wählen
        assertTrue(PhoneNumberOld.isFbTcode("*114#1234567")); //  Über das ISDN-Festnetz mit der 4. MSN die Nummer 1234567 wählen
        assertTrue(PhoneNumberOld.isFbTcode("*115#")); //  Über das ISDN-Festnetz mit der 5. MSN die folgende Nummer wählen
        assertTrue(PhoneNumberOld.isFbTcode("*115#1234567")); //  Über das ISDN-Festnetz mit der 5. MSN die Nummer 1234567 wählen
        assertTrue(PhoneNumberOld.isFbTcode("*116#")); //  Über das ISDN-Festnetz mit der 6. MSN die folgende Nummer wählen
        assertTrue(PhoneNumberOld.isFbTcode("*116#1234567")); //  Über das ISDN-Festnetz mit der 6. MSN die Nummer 1234567 wählen
        assertTrue(PhoneNumberOld.isFbTcode("*117#")); //  Über das ISDN-Festnetz mit der 7. MSN die folgende Nummer wählen
        assertTrue(PhoneNumberOld.isFbTcode("*117#1234567")); //  Über das ISDN-Festnetz mit der 7. MSN die Nummer 1234567 wählen
        assertTrue(PhoneNumberOld.isFbTcode("*118#")); //  Über das ISDN-Festnetz mit der 8. MSN die folgende Nummer wählen
        assertTrue(PhoneNumberOld.isFbTcode("*118#1234567")); //  Über das ISDN-Festnetz mit der 8. MSN die Nummer 1234567 wählen
        assertTrue(PhoneNumberOld.isFbTcode("*119#")); //  Über das ISDN-Festnetz mit der 9. MSN die folgende Nummer wählen
        assertTrue(PhoneNumberOld.isFbTcode("*119#1234567")); //  Über das ISDN-Festnetz mit der 9. MSN die Nummer 1234567 wählen

        assertTrue(PhoneNumberOld.isFbTcode("*12#")); // Über eine Internetrufnummer die folgende Nummer wählen
        assertTrue(PhoneNumberOld.isFbTcode("*12#1234567")); // Über eine Internetrufnummer die Nummer 1234567 wählen

        assertTrue(PhoneNumberOld.isFbTcode("*120#")); //  Über das ISDN-Festnetz mit der 10. MSN die folgende Nummer wählen
        assertTrue(PhoneNumberOld.isFbTcode("*120#1234567")); //  Über das ISDN-Festnetz mit der 10. MSN die Nummer 1234567 wählen
        assertTrue(PhoneNumberOld.isFbTcode("*121#")); //  Über das ISDN-Festnetz mit der 1. MSN die folgende Nummer wählen
        assertTrue(PhoneNumberOld.isFbTcode("*121#1234567")); //  Über das ISDN-Festnetz mit der 1. MSN die Nummer 1234567 wählen
        assertTrue(PhoneNumberOld.isFbTcode("*122#")); //  Über das ISDN-Festnetz mit der 2. MSN die folgende Nummer wählen
        assertTrue(PhoneNumberOld.isFbTcode("*122#1234567")); //  Über das ISDN-Festnetz mit der 2. MSN die Nummer 1234567 wählen
        assertTrue(PhoneNumberOld.isFbTcode("*123#")); //  Über das ISDN-Festnetz mit der 3. MSN die folgende Nummer wählen
        assertTrue(PhoneNumberOld.isFbTcode("*123#1234567")); //  Über das ISDN-Festnetz mit der 3. MSN die Nummer 1234567 wählen
        assertTrue(PhoneNumberOld.isFbTcode("*124#")); //  Über das ISDN-Festnetz mit der 4. MSN die folgende Nummer wählen
        assertTrue(PhoneNumberOld.isFbTcode("*124#1234567")); //  Über das ISDN-Festnetz mit der 4. MSN die Nummer 1234567 wählen
        assertTrue(PhoneNumberOld.isFbTcode("*125#")); //  Über das ISDN-Festnetz mit der 5. MSN die folgende Nummer wählen
        assertTrue(PhoneNumberOld.isFbTcode("*125#1234567")); //  Über das ISDN-Festnetz mit der 5. MSN die Nummer 1234567 wählen
        assertTrue(PhoneNumberOld.isFbTcode("*126#")); //  Über das ISDN-Festnetz mit der 6. MSN die folgende Nummer wählen
        assertTrue(PhoneNumberOld.isFbTcode("*126#1234567")); //  Über das ISDN-Festnetz mit der 6. MSN die Nummer 1234567 wählen
        assertTrue(PhoneNumberOld.isFbTcode("*127#")); //  Über das ISDN-Festnetz mit der 7. MSN die folgende Nummer wählen
        assertTrue(PhoneNumberOld.isFbTcode("*127#1234567")); //  Über das ISDN-Festnetz mit der 7. MSN die Nummer 1234567 wählen
        assertTrue(PhoneNumberOld.isFbTcode("*128#")); //  Über das ISDN-Festnetz mit der 8. MSN die folgende Nummer wählen
        assertTrue(PhoneNumberOld.isFbTcode("*128#1234567")); //  Über das ISDN-Festnetz mit der 8. MSN die Nummer 1234567 wählen
        assertTrue(PhoneNumberOld.isFbTcode("*129#")); //  Über das ISDN-Festnetz mit der 9. MSN die folgende Nummer wählen
        assertTrue(PhoneNumberOld.isFbTcode("*129#1234567")); //  Über das ISDN-Festnetz mit der 9. MSN die Nummer 1234567 wählen
        assertTrue(PhoneNumberOld.isFbTcode("*1211#")); //  Über das ISDN-Festnetz mit der 11. MSN die folgende Nummer wählen
        assertTrue(PhoneNumberOld.isFbTcode("*1211#1234567")); //  Über das ISDN-Festnetz mit der 11. MSN die Nummer 1234567 wählen
        assertTrue(PhoneNumberOld.isFbTcode("*1212#")); //  Über das ISDN-Festnetz mit der 12. MSN die folgende Nummer wählen
        assertTrue(PhoneNumberOld.isFbTcode("*1212#1234567")); //  Über das ISDN-Festnetz mit der 12. MSN die Nummer 1234567 wählen
        assertTrue(PhoneNumberOld.isFbTcode("*1213#")); //  Über das ISDN-Festnetz mit der 13. MSN die folgende Nummer wählen
        assertTrue(PhoneNumberOld.isFbTcode("*1213#1234567")); //  Über das ISDN-Festnetz mit der 13. MSN die Nummer 1234567 wählen
        assertTrue(PhoneNumberOld.isFbTcode("*1214#")); //  Über das ISDN-Festnetz mit der 14. MSN die folgende Nummer wählen
        assertTrue(PhoneNumberOld.isFbTcode("*1214#1234567")); //  Über das ISDN-Festnetz mit der 14. MSN die Nummer 1234567 wählen
        assertTrue(PhoneNumberOld.isFbTcode("*1215#")); //  Über das ISDN-Festnetz mit der 15. MSN die folgende Nummer wählen
        assertTrue(PhoneNumberOld.isFbTcode("*1215#1234567")); //  Über das ISDN-Festnetz mit der 15. MSN die Nummer 1234567 wählen
        assertTrue(PhoneNumberOld.isFbTcode("*1216#")); //  Über das ISDN-Festnetz mit der 16. MSN die folgende Nummer wählen
        assertTrue(PhoneNumberOld.isFbTcode("*1216#1234567")); //  Über das ISDN-Festnetz mit der 16. MSN die Nummer 1234567 wählen
        assertTrue(PhoneNumberOld.isFbTcode("*1217#")); //  Über das ISDN-Festnetz mit der 17. MSN die folgende Nummer wählen
        assertTrue(PhoneNumberOld.isFbTcode("*1217#1234567")); //  Über das ISDN-Festnetz mit der 17. MSN die Nummer 1234567 wählen
        assertTrue(PhoneNumberOld.isFbTcode("*1218#")); //  Über das ISDN-Festnetz mit der 18. MSN die folgende Nummer wählen
        assertTrue(PhoneNumberOld.isFbTcode("*1218#1234567")); //  Über das ISDN-Festnetz mit der 18. MSN die Nummer 1234567 wählen
        assertTrue(PhoneNumberOld.isFbTcode("*1219#")); //  Über das ISDN-Festnetz mit der 19. MSN die folgende Nummer wählen
        assertTrue(PhoneNumberOld.isFbTcode("*1219#1234567")); //  Über das ISDN-Festnetz mit der 19. MSN die Nummer 1234567 wählen
        assertTrue(PhoneNumberOld.isFbTcode("*1220#")); //  Über das ISDN-Festnetz mit der 20. MSN die folgende Nummer wählen
        assertTrue(PhoneNumberOld.isFbTcode("*1220#1234567")); //  Über das ISDN-Festnetz mit der 20. MSN die Nummer 1234567 wählen


        assertTrue(PhoneNumberOld.isFbTcode("*30#")); // Wahlregeln und LCR für den folgenden Anruf deaktivieren
        assertTrue(PhoneNumberOld.isFbTcode("#30#")); // Wahlregeln und LCR für den folgenden Anruf aktivieren
        assertTrue(PhoneNumberOld.isFbTcode("*31#")); // Rufnummernunterdrückung (CLIR) für den folgenden Anruf aktivieren
        assertTrue(PhoneNumberOld.isFbTcode("#31#")); // Rufnummernunterdrückung (CLIR) für den folgenden Anruf deaktivieren
        assertTrue(PhoneNumberOld.isFbTcode("*34#")); // Anklopfschutz für den folgenden Anruf aktivieren
        assertTrue(PhoneNumberOld.isFbTcode("#34#")); // Anklopfschutz für den folgenden Anruf deaktivieren

        assertTrue(PhoneNumberOld.isFbTcode("#564*0*")); // Wahlregeln und LCR für Callthrough deaktivieren
        assertTrue(PhoneNumberOld.isFbTcode("#564*1*")); // Wahlregeln und LCR für Callthrough aktivieren

        assertTrue(PhoneNumberOld.isFbTcode("**600")); // Anrufbeantworter

        assertTrue(PhoneNumberOld.isFbTcode("#81*1*")); // Konfigurierte Klingelsperren für alle Nebenstellen aktivieren
        assertTrue(PhoneNumberOld.isFbTcode("#81*6*")); // Konfigurierte Klingelsperren für alle Nebenstellen deaktivieren

        assertTrue(PhoneNumberOld.isFbTcode("#90*pin*")); // Wahl der Internetrufnummer für das folgende Telefonat entsperren
        assertTrue(PhoneNumberOld.isFbTcode("#91*pin*")); // Wahl der Internetrufnummer dauerhaft entsperren

        assertTrue(PhoneNumberOld.isFbTcode("#96*2*")); // CAPI-over-TCP deaktivieren
        assertTrue(PhoneNumberOld.isFbTcode("#96*3*")); // CAPI-over-TCP aktivieren
        assertTrue(PhoneNumberOld.isFbTcode("#96*4*")); // Callmonitor-Support deaktivieren
        assertTrue(PhoneNumberOld.isFbTcode("#96*5*")); // Callmonitor-Support aktivieren
        assertTrue(PhoneNumberOld.isFbTcode("#96*6*")); // Bier holen ausgeben
        assertTrue(PhoneNumberOld.isFbTcode("#96*7*")); // telnetd aktivieren
        assertTrue(PhoneNumberOld.isFbTcode("#96*8*")); // telnetd deaktivieren

        assertTrue(PhoneNumberOld.isFbTcode("#961*0*")); // Anrufweiterschaltung über Vermittlung verbieten (intern forcieren)
        assertTrue(PhoneNumberOld.isFbTcode("#961*1*")); // Anrufweiterschaltung über Vermittlung zulassen (extern erlauben)
        assertTrue(PhoneNumberOld.isFbTcode("#961*2*")); // Busy-on-Busy (BoB) für POTS deaktivieren
        assertTrue(PhoneNumberOld.isFbTcode("#961*3*")); // Busy-on-Busy (BoB) für POTS aktivieren (automatisch)
        assertTrue(PhoneNumberOld.isFbTcode("#961*4*")); // Faxweiche für POTS deaktivieren
        assertTrue(PhoneNumberOld.isFbTcode("#961*5*")); // Faxweiche für POTS aktivieren (detect)

        assertTrue(PhoneNumberOld.isFbTcode("#97*0*")); // MWI aktivieren
        assertTrue(PhoneNumberOld.isFbTcode("#97*1*")); // MWI deaktivieren
        assertTrue(PhoneNumberOld.isFbTcode("#97*2*")); // dtrace deaktivieren
        assertTrue(PhoneNumberOld.isFbTcode("#97*3*")); // dtrace aktivieren

        assertTrue(PhoneNumberOld.isFbTcode("#99**")); // Werksreset der Telefonie-Einstellungen
        assertTrue(PhoneNumberOld.isFbTcode("#990*15901590*")); // Neustart
        assertTrue(PhoneNumberOld.isFbTcode("#991*15901590*")); // Werksreset aller Einstellungen
    }
}
