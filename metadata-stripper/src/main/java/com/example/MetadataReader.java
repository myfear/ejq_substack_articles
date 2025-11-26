package com.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImagingException;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.common.ImageMetadata.ImageMetadataItem;
import org.apache.commons.imaging.common.RationalNumber;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.GpsTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;
import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MetadataReader {

    @Inject
    Logger log;

    /**
     * Reads metadata from image bytes and returns a structured representation.
     * Based on the Apache Commons Imaging MetadataExample.
     * 
     * @param imageBytes the image data as byte array
     * @return a map containing metadata information
     * @throws ImagingException if metadata cannot be read
     * @throws IOException      if I/O error occurs
     */
    public Map<String, Object> readMetadata(byte[] imageBytes) throws ImagingException, IOException {
        Map<String, Object> metadataMap = new HashMap<>();

        // Get all metadata stored in EXIF format (ie. from JPEG or TIFF)
        final ImageMetadata metadata = Imaging.getMetadata(imageBytes);

        if (metadata == null) {
            metadataMap.put("hasMetadata", false);
            return metadataMap;
        }

        metadataMap.put("hasMetadata", true);
        metadataMap.put("metadataType", metadata.getClass().getSimpleName());

        if (metadata instanceof JpegImageMetadata) {
            final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;

            // Extract common EXIF tags
            Map<String, String> exifTags = new HashMap<>();
            extractTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_XRESOLUTION, exifTags);
            extractTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_YRESOLUTION, exifTags);
            extractTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_DATE_TIME, exifTags);
            extractTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_MAKE, exifTags);
            extractTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_MODEL, exifTags);
            extractTagValue(jpegMetadata, TiffTagConstants.TIFF_TAG_ORIENTATION, exifTags);
            extractTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL, exifTags);
            extractTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED, exifTags);
            extractTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_ISO, exifTags);
            extractTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_SHUTTER_SPEED_VALUE, exifTags);
            extractTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_APERTURE_VALUE, exifTags);
            extractTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_BRIGHTNESS_VALUE, exifTags);
            extractTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_FOCAL_LENGTH, exifTags);
            extractTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_LENS_MAKE, exifTags);
            extractTagValue(jpegMetadata, ExifTagConstants.EXIF_TAG_LENS_MODEL, exifTags);

            metadataMap.put("exifTags", exifTags);

            // Extract GPS information
            Map<String, Object> gpsInfo = extractGpsInfo(jpegMetadata);
            if (!gpsInfo.isEmpty()) {
                metadataMap.put("gps", gpsInfo);
            }

            // Extract all metadata items
            List<String> allItems = new ArrayList<>();
            final List<ImageMetadataItem> items = jpegMetadata.getItems();
            for (final ImageMetadataItem item : items) {
                allItems.add(item.toString());
            }
            metadataMap.put("allItems", allItems);
        }

        return metadataMap;
    }

    /**
     * Extracts a specific tag value from JPEG metadata.
     */
    private void extractTagValue(final JpegImageMetadata jpegMetadata, final TagInfo tagInfo,
            Map<String, String> exifTags) {
        final TiffField field = jpegMetadata.findExifValueWithExactMatch(tagInfo);
        if (field != null) {
            exifTags.put(tagInfo.name, field.getValueDescription());
        }
    }

    /**
     * Extracts GPS information from JPEG metadata.
     */
    private Map<String, Object> extractGpsInfo(final JpegImageMetadata jpegMetadata) {
        Map<String, Object> gpsMap = new HashMap<>();

        try {
            // Simple interface to GPS data
            final TiffImageMetadata exifMetadata = jpegMetadata.getExif();
            if (exifMetadata != null) {
                final TiffImageMetadata.GpsInfo gpsInfo = exifMetadata.getGpsInfo();
                if (gpsInfo != null) {
                    gpsMap.put("description", gpsInfo.toString());
                    gpsMap.put("longitude", gpsInfo.getLongitudeAsDegreesEast());
                    gpsMap.put("latitude", gpsInfo.getLatitudeAsDegreesNorth());
                }
            }
        } catch (ImagingException e) {
            log.debugf("Failed to extract GPS info using simple interface: %s", e.getMessage());
        }

        // More specific GPS field extraction
        try {
            final TiffField gpsLatitudeRefField = jpegMetadata
                    .findExifValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LATITUDE_REF);
            final TiffField gpsLatitudeField = jpegMetadata
                    .findExifValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LATITUDE);
            final TiffField gpsLongitudeRefField = jpegMetadata
                    .findExifValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LONGITUDE_REF);
            final TiffField gpsLongitudeField = jpegMetadata
                    .findExifValueWithExactMatch(GpsTagConstants.GPS_TAG_GPS_LONGITUDE);

            if (gpsLatitudeRefField != null && gpsLatitudeField != null &&
                    gpsLongitudeRefField != null && gpsLongitudeField != null) {

                try {
                    final String gpsLatitudeRef = (String) gpsLatitudeRefField.getValue();
                    final RationalNumber[] gpsLatitude = (RationalNumber[]) gpsLatitudeField.getValue();
                    final String gpsLongitudeRef = (String) gpsLongitudeRefField.getValue();
                    final RationalNumber[] gpsLongitude = (RationalNumber[]) gpsLongitudeField.getValue();

                    if (gpsLatitude != null && gpsLatitude.length >= 3 &&
                            gpsLongitude != null && gpsLongitude.length >= 3) {

                        Map<String, Object> detailedGps = new HashMap<>();
                        detailedGps.put("latitudeRef", gpsLatitudeRef);
                        detailedGps.put("latitudeDegrees", gpsLatitude[0].toDisplayString());
                        detailedGps.put("latitudeMinutes", gpsLatitude[1].toDisplayString());
                        detailedGps.put("latitudeSeconds", gpsLatitude[2].toDisplayString());
                        detailedGps.put("longitudeRef", gpsLongitudeRef);
                        detailedGps.put("longitudeDegrees", gpsLongitude[0].toDisplayString());
                        detailedGps.put("longitudeMinutes", gpsLongitude[1].toDisplayString());
                        detailedGps.put("longitudeSeconds", gpsLongitude[2].toDisplayString());

                        gpsMap.put("detailed", detailedGps);
                    }
                } catch (ClassCastException e) {
                    log.debugf("GPS data format is unexpected, cannot parse detailed GPS info: %s", e.getMessage());
                }
            }
        } catch (ImagingException e) {
            log.debugf("Failed to find GPS fields: %s", e.getMessage());
        }

        return gpsMap;
    }

    /**
     * Checks if the image has any metadata.
     * 
     * @param imageBytes the image data as byte array
     * @return true if metadata is present, false otherwise
     */
    public boolean hasMetadata(byte[] imageBytes) {
        try {
            final ImageMetadata metadata = Imaging.getMetadata(imageBytes);
            return metadata != null;
        } catch (IOException e) {
            log.debugf("Failed to check if image has metadata: %s", e.getMessage());
            return false;
        }
    }
}
