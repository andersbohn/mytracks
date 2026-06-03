package com.andersbohn.mytracks.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.garmin.fit.BufferEncoder;
import com.garmin.fit.DateTime;
import com.garmin.fit.FileIdMesg;
import com.garmin.fit.Fit;
import com.garmin.fit.SessionMesg;
import com.garmin.fit.Sport;
import com.garmin.fit.SubSport;
import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.Test;

class FitParserTest {

  @Test
  void parse_extractsSessionFields() throws Exception {
    byte[] fitBytes = buildMinimalFit();

    var meta = FitParser.parse(new ByteArrayInputStream(fitBytes));

    assertThat(meta.durationSeconds()).isEqualTo(3600);
    assertThat(meta.movingTimeSeconds()).isEqualTo(3500);
    assertThat(meta.distanceMeters()).isEqualTo(15000.0);
    assertThat(meta.avgHeartRate()).isEqualTo(145);
    assertThat(meta.maxHeartRate()).isEqualTo(180);
    assertThat(meta.calories()).isEqualTo(500);
    assertThat(meta.avgPowerWatts()).isEqualTo(200);
    assertThat(meta.normalizedPowerWatts()).isEqualTo(210);
    assertThat(meta.avgCadence()).isEqualTo(85);
    assertThat(meta.sport()).isEqualTo("CYCLING");
    assertThat(meta.subSport()).isEqualTo("ROAD");
    assertThat(meta.ascentMeters()).isEqualTo(500.0);
    assertThat(meta.descentMeters()).isEqualTo(480.0);
    assertThat(meta.startTime()).isNotNull();
  }

  @Test
  void parse_emptySession_returnsNullFields() throws Exception {
    byte[] fitBytes = buildEmptySessionFit();

    var meta = FitParser.parse(new ByteArrayInputStream(fitBytes));

    assertThat(meta.durationSeconds()).isNull();
    assertThat(meta.sport()).isNull();
  }

  private static byte[] buildMinimalFit() throws Exception {
    var encoder = new BufferEncoder(Fit.ProtocolVersion.V2_0);
    encoder.open();

    var fileId = new FileIdMesg();
    fileId.setType(com.garmin.fit.File.ACTIVITY);
    encoder.write(fileId);

    var session = new SessionMesg();
    session.setStartTime(new DateTime(Date.from(Instant.parse("2024-06-01T08:00:00Z"))));
    session.setTotalElapsedTime(3600.0f);
    session.setTotalTimerTime(3500.0f);
    session.setTotalDistance(15000.0f);
    session.setTotalCalories(500);
    session.setAvgHeartRate((short) 145);
    session.setMaxHeartRate((short) 180);
    session.setAvgSpeed(4.1667f);
    session.setMaxSpeed(6.0f);
    session.setAvgPower(200);
    session.setNormalizedPower(210);
    session.setAvgCadence((short) 85);
    session.setSport(Sport.CYCLING);
    session.setSubSport(SubSport.ROAD);
    session.setTotalAscent(500);
    session.setTotalDescent(480);
    encoder.write(session);

    return encoder.close();
  }

  private static byte[] buildEmptySessionFit() throws Exception {
    var encoder = new BufferEncoder(Fit.ProtocolVersion.V2_0);
    encoder.open();

    var fileId = new FileIdMesg();
    fileId.setType(com.garmin.fit.File.ACTIVITY);
    encoder.write(fileId);

    encoder.write(new SessionMesg());
    return encoder.close();
  }
}
