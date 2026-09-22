package net.privactivity.fit.decode;

import com.garmin.fit.ActivityMesg;
import com.garmin.fit.ActivityMesgListener;
import com.garmin.fit.Decode;
import com.garmin.fit.FileIdMesg;
import com.garmin.fit.FileIdMesgListener;
import com.garmin.fit.FitRuntimeException;
import com.garmin.fit.HrvMesg;
import com.garmin.fit.HrvMesgListener;
import com.garmin.fit.LapMesg;
import com.garmin.fit.LapMesgListener;
import com.garmin.fit.MesgBroadcaster;
import com.garmin.fit.RecordMesg;
import com.garmin.fit.RecordMesgListener;
import com.garmin.fit.SessionMesg;
import com.garmin.fit.SessionMesgListener;
import com.garmin.fit.SportMesg;
import com.garmin.fit.SportMesgListener;
import net.privactivity.domain.Activity;
import net.privactivity.fit.domain.GarminSession;
import net.privactivity.fit.domain.Record;
import net.privactivity.fit.domain.TrainingData;
import net.privactivity.store.usecase.importactivities.adapter.FitDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class FitDecoderImpl implements FitDecoder {

    private static final Logger log = LoggerFactory.getLogger(FitDecoderImpl.class);


    @Override
    public Activity extractFit(InputStream is, long id) {
        TrainingData trainingData = readTrainingData(is);
        trainingData.setId(id);
        FitToPrivactivity fitToPrivactivity = new FitToPrivactivity();
        return fitToPrivactivity.convert(trainingData);
    }

    TrainingData readTrainingData(InputStream in) {
        TrainingData trainingData = new TrainingData(); //read garmin data

        Decode decode = new Decode();
        //decode.skipHeader();        // Use on streams with no header and footer (stream contains FIT defn and data
        // messages only)
        //decode.incompleteStream();  // This suppresses exceptions with unexpected eof (also incorrect crc)
        MesgBroadcaster mesgBroadcaster = new MesgBroadcaster(decode);
        Listener listener = new Listener(trainingData);


        mesgBroadcaster.addListener((FileIdMesgListener) listener);
        mesgBroadcaster.addListener((RecordMesgListener) listener);
        mesgBroadcaster.addListener((LapMesgListener) listener);
        mesgBroadcaster.addListener((SessionMesgListener) listener);

        try {
            decode.read(in, mesgBroadcaster, mesgBroadcaster);
        } catch (FitRuntimeException e) {
            // If a file with 0 data size in its header  has been encountered,
            // attempt to keep processing the file
            if (decode.getInvalidFileDataSize()) {
                decode.nextFile();
                decode.read(in, mesgBroadcaster, mesgBroadcaster);
            } else {
                throw e;
            }
        }
        listener.assignLapNumbersToRecords();
        return trainingData;
    }

    private static class Listener implements SessionMesgListener, RecordMesgListener, FileIdMesgListener,
            LapMesgListener,
            SportMesgListener,
            ActivityMesgListener, HrvMesgListener {
        TrainingData trainingData;
        List<LapBoundary> lapBoundaries = new ArrayList<>();

        public Listener(TrainingData trainingData) {
            this.trainingData = trainingData;
        }

        @Override
        public void onMesg(RecordMesg mesg) {
            Record record = new Record();
            record.setHeartRate(mesg.getHeartRate());
            record.setCadence(mesg.getCadence());

            if (mesg.getDistance() != null) {
                record.setDistance(BigDecimal.valueOf(mesg.getDistance())
                                             .setScale(2, RoundingMode.HALF_UP).doubleValue());  // 110.03
            }
            if (mesg.getEnhancedSpeed() != null) {
                record.setSpeed(BigDecimal.valueOf(mesg.getEnhancedSpeed())
                                          .setScale(3, RoundingMode.HALF_UP).doubleValue());
            } else if (mesg.getSpeed() != null) {
                record.setSpeed(BigDecimal.valueOf(mesg.getSpeed())
                                          .setScale(3, RoundingMode.HALF_UP).doubleValue()); // 5.618
            }
            if (mesg.getEnhancedAltitude() != null) {
                record.setAltitude(BigDecimal.valueOf(mesg.getEnhancedAltitude())
                                             .setScale(1, RoundingMode.HALF_UP).doubleValue());
            } else if (mesg.getAltitude() != null) {
                record.setAltitude(BigDecimal.valueOf(mesg.getAltitude())
                                             .setScale(1, RoundingMode.HALF_UP).doubleValue()); //318.4
            }

            record.setTemperature(mesg.getTemperature());
            if (mesg.getTimestamp() != null) {
                record.setTimestamp(mesg.getTimestamp().getDate());
            }

            if (mesg.getGrade() != null) {
                record.setGrade(BigDecimal.valueOf(mesg.getGrade())
                                          .setScale(2, RoundingMode.HALF_UP).doubleValue()); // 0.88
            }

            record.setLat(mesg.getPositionLat());
            record.setLon(mesg.getPositionLong());
            record.setPower(mesg.getPower());

            trainingData.addRecord(record);
        }

        @Override
        public void onMesg(SessionMesg mesg) {

            GarminSession garminSession = new GarminSession();

            if (mesg.getEnhancedMaxAltitude() != null) {
                garminSession.setMaxAltitude(BigDecimal.valueOf(mesg.getEnhancedMaxAltitude())
                                                       .setScale(1, RoundingMode.HALF_UP).doubleValue());
            } else if (mesg.getMaxAltitude() != null) {
                garminSession.setMaxAltitude(BigDecimal.valueOf(mesg.getMaxAltitude())
                                                       .setScale(1, RoundingMode.HALF_UP).doubleValue());
            }

            garminSession.setMaxCadence(mesg.getMaxCadence());
            garminSession.setMaxHeartRate(mesg.getMaxHeartRate());

            if (mesg.getEnhancedMaxSpeed() != null) {
                garminSession.setMaxSpeed(BigDecimal.valueOf(mesg.getEnhancedMaxSpeed())
                                                    .setScale(3, RoundingMode.HALF_UP).doubleValue());
            } else if (mesg.getMaxSpeed() != null) {
                garminSession.setMaxSpeed(BigDecimal.valueOf(mesg.getMaxSpeed())
                                                    .setScale(3, RoundingMode.HALF_UP).doubleValue());
            }
            garminSession.setMaxTemperature(mesg.getMaxTemperature());

            if (mesg.getEnhancedAvgAltitude() != null) {
                garminSession.setAvgAltitude(BigDecimal.valueOf(mesg.getEnhancedAvgAltitude())
                                                       .setScale(1, RoundingMode.HALF_UP).doubleValue());
            } else if (mesg.getAvgAltitude() != null) {
                garminSession.setAvgAltitude(BigDecimal.valueOf(mesg.getAvgAltitude())
                                                       .setScale(1, RoundingMode.HALF_UP).doubleValue());
            }
            garminSession.setAvgCadence(mesg.getAvgCadence());
            garminSession.setAvgHeartRate(mesg.getAvgHeartRate());
            if (mesg.getEnhancedAvgSpeed() != null) {
                garminSession.setAvgSpeed(BigDecimal.valueOf(mesg.getEnhancedAvgSpeed())
                                                    .setScale(3, RoundingMode.HALF_UP).doubleValue());
            } else if (mesg.getAvgSpeed() != null) {
                garminSession.setAvgSpeed(BigDecimal.valueOf(mesg.getAvgSpeed())
                                                    .setScale(3, RoundingMode.HALF_UP).doubleValue());
            }
            garminSession.setAvgTemperature(mesg.getAvgTemperature());

            garminSession.setTotalAscent(mesg.getTotalAscent());
            garminSession.setTotalDescent(mesg.getTotalDescent());
            if (mesg.getTotalDistance() != null) {
                garminSession.setTotalDistance(BigDecimal.valueOf(mesg.getTotalDistance())
                                                         .setScale(2, RoundingMode.HALF_UP).doubleValue());
            }
            if (mesg.getTotalMovingTime() != null) {
                garminSession.setTotalMovingTime(BigDecimal.valueOf(mesg.getTotalMovingTime())
                                                           .setScale(1, RoundingMode.HALF_UP).doubleValue());
            }
            if (mesg.getTotalElapsedTime() != null) {
                garminSession.setTotalElapsedTime(BigDecimal.valueOf(mesg.getTotalElapsedTime())
                                                            .setScale(1, RoundingMode.HALF_UP).doubleValue());
            }

            garminSession.setAvgPower(mesg.getAvgPower());
            garminSession.setMaxPower(mesg.getMaxPower());
            garminSession.setThresholdPower(mesg.getThresholdPower());
            garminSession.setNormalizedPower(mesg.getNormalizedPower());
            garminSession.setTotalTrainingEffect(mesg.getTotalTrainingEffect());
            garminSession.setTrainingStressScore(mesg.getTrainingStressScore());
            garminSession.setTotalCalories(mesg.getTotalCalories());
            garminSession.setIntensityFactor(mesg.getIntensityFactor());
            garminSession.setAvgLeftPco(mesg.getAvgLeftPco());
            garminSession.setAvgRightPco(mesg.getAvgRightPco());
            if (mesg.getSport() != null) {
                String sport = mesg.getSport().name();
                if (mesg.getSubSport() != null) {
                    sport = mesg.getSubSport().name() + " " + sport;
                }
                garminSession.setSport(sport);
            }

            trainingData.setGarminSession(garminSession);
            if (mesg.getStartTime() != null) {
                trainingData.setDate(mesg.getStartTime().getDate());
            }
        }

        @Override
        public void onMesg(LapMesg mesg) {
            Date lapStartTime = mesg.getStartTime() != null ? mesg.getStartTime().getDate() : null;
            Integer lapNumber = mesg.getMessageIndex() != null ? mesg.getMessageIndex() + 1 : lapBoundaries.size() + 1;
            if (lapStartTime != null) {
                lapBoundaries.add(new LapBoundary(lapNumber, lapStartTime));
            }
        }

        @Override
        public void onMesg(FileIdMesg mesg) {
            // seen many many times
        }

        @Override
        public void onMesg(SportMesg mesg) {
            log.debug("SportMesg: {}", mesg);
        }

        @Override
        public void onMesg(ActivityMesg mesg) {
            log.debug("ActivityMesg: {}", mesg);
        }

        @Override
        public void onMesg(HrvMesg mesg) {
            log.debug("HrvMesg: {}", mesg);
        }

        private void assignLapNumbersToRecords() {
            if (lapBoundaries.isEmpty()) {
                return;
            }
            List<LapBoundary> sortedLaps = lapBoundaries.stream()
                                                        .sorted(Comparator.comparing(l -> l.startTime))
                                                        .toList();
            for (Record record : trainingData.getRecords()) {
                Date recordTimestamp = record.getTimestamp();
                if (recordTimestamp == null) {
                    continue;
                }
                Integer lapNumber = null;
                for (LapBoundary lapBoundary : sortedLaps) {
                    if (recordTimestamp.before(lapBoundary.startTime)) {
                        break;
                    }
                    lapNumber = lapBoundary.lapNumber;
                }
                if (lapNumber != null) {
                    record.setLapNumber(lapNumber);
                }
            }
        }
    }

    private record LapBoundary(Integer lapNumber, Date startTime) {
    }

}
