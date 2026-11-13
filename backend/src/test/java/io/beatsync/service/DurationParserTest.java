package io.beatsync.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class DurationParserTest {

    @Test
    void parsesMinutesAndSeconds() {
        assertThat(DurationParser.toSeconds("4:32")).isEqualTo(272);
        assertThat(DurationParser.toSeconds("0:09")).isEqualTo(9);
    }

    @Test
    void parsesHours() {
        assertThat(DurationParser.toSeconds("1:02:03")).isEqualTo(3723);
    }

    @Test
    void rejectsJunk() {
        assertThatThrownBy(() -> DurationParser.toSeconds("fast"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
