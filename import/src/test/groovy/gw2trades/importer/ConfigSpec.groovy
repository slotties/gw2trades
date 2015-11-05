package gw2trades.importer

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
class ConfigSpec extends Specification {
    @Subject
    Config config

    @Unroll("required: #key")
    def required(List<String> key, String expectedValue, boolean expectedException) {
        given:
        config = new Config([
                "foo": [
                        "bar": [
                                "bla": "blub"
                        ]
                ],
                "hehe": "hrhr"
        ]);

        when:
        String value
        boolean encouteredException
        try {
            value = config.required(key.toArray(new String[key.size()]))
            encouteredException = false
        } catch (IllegalArgumentException e) {
            value = null
            encouteredException = true
        }

        then:
        encouteredException == expectedException
        value == expectedValue

        where:
        key                      | expectedValue | expectedException
        ["foo"]                  | null          | true
        ["foo", "bar"]           | null          | true
        ["foo", "bar", "bla"]    | "blub"        | false
        ["hehe"]                 | "hrhr"        | false
        ["doesNotExist"]         | null          | true
        ["does", "not", "exist"] | null          | true
    }

    @Unroll("optional: #key")
    def optional(List<String> key, String expectedValue) {
        given:
        config = new Config([
                "foo": [
                        "bar": [
                                "bla": "blub"
                        ]
                ],
                "hehe": "hrhr"
        ])

        when:
        Optional<String> value = config.optional(key.toArray(new String[key.size()]))

        then:
        value.orElse(null) == expectedValue

        where:
        key                      | expectedValue
        ["foo"]                  | null
        ["foo", "bar"]           | null
        ["foo", "bar", "bla"]    | "blub"
        ["hehe"]                 | "hrhr"
        ["doesNotExist"]         | null
        ["does", "not", "exist"] | null
    }
}
