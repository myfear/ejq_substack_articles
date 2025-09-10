package org.acme.todo;

import static org.junit.jupiter.api.Assertions.*;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.StringLength;

class SluggerProperties {

    @Property
    void onlySafeChars(@ForAll @StringLength(max = 200) String any) {
        String slug = Slugger.slugify(any);
        assertTrue(slug.matches("[a-z0-9-]*"));
    }

    @Property
    void trimmedAndBounded(@ForAll String any) {
        String slug = Slugger.slugify("  " + any + "  ");
        assertTrue(slug.length() <= 100);
    }

    @Property
    void emptyOrNullBecomesEmpty(@ForAll("empties") String s) {
        assertTrue(Slugger.slugify(s).isEmpty());
    }

    @Provide
    Arbitrary<String> empties() {
        return Arbitraries.of("", "     ", null);
    }
}