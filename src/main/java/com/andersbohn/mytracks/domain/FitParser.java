package com.andersbohn.mytracks.domain;

import com.garmin.fit.Decode;
import com.garmin.fit.MesgBroadcaster;
import com.garmin.fit.SessionMesg;
import com.garmin.fit.SessionMesgListener;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;

public class FitParser {

  public static FitMetadata parse(InputStream is) throws IOException {
    var listener = new Listener();
    var broadcaster = new MesgBroadcaster();
    broadcaster.addListener((SessionMesgListener) listener);
    var decode = new Decode();
    decode.read(is, broadcaster, broadcaster);
    return listener.build();
  }

  private static class Listener implements SessionMesgListener {
    private FitMetadata result;

    @Override
    public void onMesg(SessionMesg m) {
      Instant startTime = m.getStartTime() != null ? m.getStartTime().getDate().toInstant() : null;
      result =
          new FitMetadata(
              startTime,
              floatToInt(m.getTotalElapsedTime()),
              floatToInt(m.getTotalTimerTime()),
              floatToDouble(m.getTotalDistance()),
              shortToDouble(m.getTotalAscent()),
              shortToDouble(m.getTotalDescent()),
              shortToInt(m.getAvgHeartRate()),
              shortToInt(m.getMaxHeartRate()),
              floatToDouble(m.getAvgSpeed()),
              floatToDouble(m.getMaxSpeed()),
              intVal(m.getTotalCalories()),
              intVal(m.getAvgPower()),
              intVal(m.getNormalizedPower()),
              shortToInt(m.getAvgCadence()),
              m.getSport() != null ? m.getSport().name() : null,
              m.getSubSport() != null ? m.getSubSport().name() : null);
    }

    FitMetadata build() {
      return result != null
          ? result
          : new FitMetadata(
              null, null, null, null, null, null, null, null, null, null, null, null, null, null,
              null, null);
    }
  }

  private static Integer floatToInt(Float v) {
    return v != null ? v.intValue() : null;
  }

  private static Double floatToDouble(Float v) {
    return v != null ? v.doubleValue() : null;
  }

  private static Integer shortToInt(Short v) {
    return v != null ? (int) v : null;
  }

  private static Double shortToDouble(Integer v) {
    return v != null ? v.doubleValue() : null;
  }

  private static Integer intVal(Integer v) {
    return v;
  }
}
