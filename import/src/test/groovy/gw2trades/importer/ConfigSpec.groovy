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
    def required(String key, String expectedValue, boolean expectedException) {
        given:
        def props = new Properties()
        props.setProperty("foo", "bar")
        config = new Config(props)
        System.setProperty("bla", "blub")

        when:
        String value
        boolean encouteredException
        try {
            value = config.required(key)
            encouteredException = false
        } catch (IllegalArgumentException e) {
            value = null
            encouteredException = true
        }

        then:
        encouteredException == expectedException
        value == expectedValue

        where:
        key            | expectedValue | expectedException
        "foo"          | "bar"         | false
        "bla"          | "blub"        | false
        "doesNotExist" | null          | true
    }

    @Unroll("optional: #key")
    def optional(String key, String expectedValue) {
        given:
        def props = new Properties()
        props.setProperty("foo", "bar")
        config = new Config(props)
        System.setProperty("bla", "blub")

        when:
        Optional<String> value = config.optional(key)

        then:
        value.orElse(null) == expectedValue

        where:
        key       | expectedValue
        "foo"     | "bar"
        "foo.bar" | null
        "bla"     | "blub"
    }
}
