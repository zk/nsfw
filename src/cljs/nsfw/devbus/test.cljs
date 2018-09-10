(ns nsfw.devbus.test)

(def data
  '{:state-items
    ({:key "default-view.ui",
      :value
      {:body "The quick brown fox jumps over the lazy dog.",
       :start-ts 1532372016713,
       :duration-ms 5400000,
       :debit-credit -120},
      :updated-at 1532372116992}
     {:key ".!notes",
      :value
      {:initial
       {:slider {:visible? true},
        :new-note
        {:ui
         {:body "The quick brown fox jumps over the lazy dog.",
          :start-ts 1532372016713,
          :duration-ms 5400000,
          :debit-credit -120}}},
       :id->note
       {"note/c914561efdba4749afc8045e7ffe3789"
        {:images
         {"99D53A1F-FEEF-40E1-8BB3-7DD55A43C8B7/L0/001"
          {:modification-date "1441224147",
           :path
           "/Users/zk/Library/Developer/CoreSimulator/Devices/F95E235C-1F68-4645-B343-A9263EB961F7/data/Containers/Data/Application/AF473EE8-43AC-4EAA-8E7F-18DDB95AA9A3/tmp/react-native-image-crop-picker/E0907704-2A26-4CE9-91D6-21B8F62FA223.jpg",
           :upload-progress 100,
           :exif
           {"DPIWidth" 72,
            "{IPTC}"
            {"Byline" ["Nicolas Cornet"],
             "DigitalCreationDate" "20120808",
             "ObjectName" "Berunes",
             "DigitalCreationTime" "142949",
             "DateCreated" "20120808",
             "TimeCreated" "142949",
             "CopyrightNotice" "Nicolas Cornet"},
            "Orientation" 1,
            "{GPS}"
            {"LongitudeRef" "W",
             "Longitude" 14.538611666666666,
             "SpeedRef" "K",
             "GPSVersion" [2 3 0 0],
             "Altitude" 182.33333333333334,
             "Speed" 0.8999988197665498,
             "LatitudeRef" "N",
             "MapDatum" "WGS-84",
             "ImgDirectionRef" "T",
             "Latitude" 64.752895,
             "ImgDirection" 116.36669970267592},
            "PixelWidth" 1668,
            "ColorModel" "RGB",
            "{Exif}"
            {"ExposureMode" 1,
             "FlashPixVersion" [1 0],
             "PixelXDimension" 1668,
             "SubjectDistance" 1.33,
             "ExposureBiasValue" 0,
             "PixelYDimension" 2500,
             "SceneCaptureType" 0,
             "SubsecTimeDigitized" "8",
             "Sharpness" 0,
             "Saturation" 0,
             "FocalLength" 32,
             "LightSource" 0,
             "Contrast" 0,
             "FocalLenIn35mmFilm" 32,
             "DateTimeDigitized" "2012:08:08 14:29:49",
             "ExposureTime" 0.8,
             "SensingMethod" 2,
             "Flash" 16,
             "ColorSpace" 1,
             "ExposureProgram" 1,
             "MaxApertureValue" 4,
             "DigitalZoomRatio" 1,
             "FocalPlaneXResolution" 204.840206185567,
             "FileSource" 3,
             "SubjectDistRange" 0,
             "ISOSpeedRatings" [50],
             "FocalPlaneResolutionUnit" 4,
             "DateTimeOriginal" "2012:08:08 14:29:49",
             "WhiteBalance" 0,
             "GainControl" 0,
             "ApertureValue" 8.918862690707352,
             "CustomRendered" 0,
             "SubsecTimeOriginal" "8",
             "FocalPlaneYResolution" 204.840206185567,
             "ShutterSpeedValue" 0.32192799761975605,
             "ComponentsConfiguration" [1 2 3 0],
             "MeteringMode" 5,
             "ExifVersion" [2 3],
             "FNumber" 22},
            "{TIFF}"
            {"Software" "Aperture 3.4.5",
             "Orientation" 1,
             "Artist" "Nicolas Cornet",
             "Make" "NIKON CORPORATION",
             "Model" "NIKON D800E",
             "XResolution" 72,
             "YResolution" 72,
             "Copyright" "Nicolas Cornet",
             "DateTime" "2012:08:08 14:29:49",
             "ResolutionUnit" 2},
            "{JFIF}"
            {"XDensity" 72,
             "YDensity" 72,
             "JFIFVersion" [1 0 1],
             "DensityUnit" 1},
            "DPIHeight" 72,
            "PixelHeight" 2500,
            "{ExifAux}"
            {"LensModel" "16.0-35.0 mm f/4.0",
             "SerialNumber" "6001440",
             "ImageNumber" 10075,
             "LensInfo" [16 35 4 4],
             "LensID" 163},
            "Depth" 8,
            "ProfileName" "sRGB IEC61966-2.1"},
           :upload-start-at 1532159342404,
           :upload-id "2",
           :width 1668,
           :public-url
           "https://s3-us-west-2.amazonaws.com/nalopastures/images/99d53a1f-feef-40e1-8bb3-7dd55a43c8b7-l0-001",
           :s3-bucket "nalopastures",
           :upload-success-at 1532159345140,
           :mime "image/jpeg",
           :size 1253001,
           :source-url
           "file:///Users/zk/Library/Developer/CoreSimulator/Devices/F95E235C-1F68-4645-B343-A9263EB961F7/data/Media/DCIM/100APPLE/IMG_0004.JPG",
           :s3-region "us-west-2",
           :filename "IMG_0004.JPG",
           :creation-date "1344461390",
           :s3-key "images/99d53a1f-feef-40e1-8bb3-7dd55a43c8b7-l0-001",
           :local-identifier
           "99D53A1F-FEEF-40E1-8BB3-7DD55A43C8B7/L0/001",
           :crop-rect nil,
           :height 2500,
           :data nil},
          "9F983DBA-EC35-42B8-8773-B597CF782EDD/L0/001"
          {:modification-date "1441224147",
           :path
           "/Users/zk/Library/Developer/CoreSimulator/Devices/F95E235C-1F68-4645-B343-A9263EB961F7/data/Containers/Data/Application/AF473EE8-43AC-4EAA-8E7F-18DDB95AA9A3/tmp/react-native-image-crop-picker/7ED71548-F1E8-401C-88DB-517E99BA90A3.jpg",
           :upload-progress 100,
           :exif
           {"DPIWidth" 72,
            "{IPTC}"
            {"DigitalCreationTime" "115211",
             "DigitalCreationDate" "20120808",
             "Province/State" "Northeast",
             "Byline" ["Nicolas Cornet"],
             "ObjectName" "Godafoss",
             "TimeCreated" "115211",
             "Country/PrimaryLocationName" "Iceland",
             "DateCreated" "20120808",
             "SubLocation" "Godafoss",
             "City" "Ljósavatn",
             "CopyrightNotice" "Nicolas Cornet"},
            "Orientation" 1,
            "{GPS}"
            {"LongitudeRef" "W",
             "Longitude" 17.548928333333333,
             "SpeedRef" "K",
             "GPSVersion" [2 3 0 0],
             "Altitude" 103,
             "Speed" 1.6,
             "LatitudeRef" "N",
             "MapDatum" "WGS-84",
             "ImgDirectionRef" "T",
             "Latitude" 65.682895,
             "ImgDirection" 302.4},
            "PixelWidth" 3000,
            "ColorModel" "RGB",
            "{Exif}"
            {"ExposureMode" 1,
             "FlashPixVersion" [1 0],
             "PixelXDimension" 3000,
             "SubjectDistance" 2.99,
             "ExposureBiasValue" 0,
             "PixelYDimension" 2002,
             "SceneCaptureType" 0,
             "SubsecTimeDigitized" "9",
             "Sharpness" 0,
             "Saturation" 0,
             "FocalLength" 24,
             "LightSource" 0,
             "Contrast" 0,
             "FocalLenIn35mmFilm" 24,
             "DateTimeDigitized" "2012:08:08 11:52:11",
             "ExposureTime" 4,
             "SensingMethod" 2,
             "Flash" 16,
             "ColorSpace" 1,
             "ExposureProgram" 1,
             "MaxApertureValue" 4,
             "DigitalZoomRatio" 1,
             "FocalPlaneXResolution" 204.840206185567,
             "FileSource" 3,
             "SubjectDistRange" 0,
             "ISOSpeedRatings" [200],
             "FocalPlaneResolutionUnit" 4,
             "DateTimeOriginal" "2012:08:08 11:52:11",
             "WhiteBalance" 0,
             "GainControl" 0,
             "ApertureValue" 6.643855776306108,
             "CustomRendered" 0,
             "SubsecTimeOriginal" "9",
             "FocalPlaneYResolution" 204.840206185567,
             "ShutterSpeedValue" -2,
             "ComponentsConfiguration" [1 2 3 0],
             "MeteringMode" 5,
             "ExifVersion" [2 3],
             "FNumber" 10},
            "{TIFF}"
            {"Software" "Aperture 3.4.5",
             "Orientation" 1,
             "Artist" "Nicolas Cornet",
             "Make" "NIKON CORPORATION",
             "Model" "NIKON D800E",
             "XResolution" 72,
             "YResolution" 72,
             "Copyright" "Nicolas Cornet",
             "DateTime" "2012:08:08 11:52:11",
             "ResolutionUnit" 2},
            "DPIHeight" 72,
            "PixelHeight" 2002,
            "{ExifAux}"
            {"LensModel" "24.0-120.0 mm f/4.0",
             "SerialNumber" "6001440",
             "ImageNumber" 8458,
             "LensInfo" [24 120 4 4],
             "LensID" 170},
            "Depth" 8,
            "ProfileName" "sRGB IEC61966-2.1"},
           :upload-start-at 1532159345145,
           :upload-id "3",
           :width 3000,
           :public-url
           "https://s3-us-west-2.amazonaws.com/nalopastures/images/9f983dba-ec35-42b8-8773-b597cf782edd-l0-001",
           :s3-bucket "nalopastures",
           :upload-success-at 1532159347280,
           :mime "image/jpeg",
           :size 2475488,
           :source-url
           "file:///Users/zk/Library/Developer/CoreSimulator/Devices/F95E235C-1F68-4645-B343-A9263EB961F7/data/Media/DCIM/100APPLE/IMG_0003.JPG",
           :s3-region "us-west-2",
           :filename "IMG_0003.JPG",
           :creation-date "1344451932",
           :s3-key "images/9f983dba-ec35-42b8-8773-b597cf782edd-l0-001",
           :local-identifier
           "9F983DBA-EC35-42B8-8773-B597CF782EDD/L0/001",
           :crop-rect nil,
           :height 2002,
           :data nil},
          "B84E8479-475C-4727-A4A4-B77AA9980897/L0/001"
          {:modification-date "1441224147",
           :path
           "/Users/zk/Library/Developer/CoreSimulator/Devices/F95E235C-1F68-4645-B343-A9263EB961F7/data/Containers/Data/Application/AF473EE8-43AC-4EAA-8E7F-18DDB95AA9A3/tmp/react-native-image-crop-picker/E031E630-C6D1-470B-BA02-707BCB8B1413.jpg",
           :upload-progress 100,
           :exif
           {"DPIWidth" 72,
            "{IPTC}"
            {"DigitalCreationTime" "140920",
             "DigitalCreationDate" "20091009",
             "ObjectName" "DSC_0010",
             "DateCreated" "20091009",
             "TimeCreated" "140920"},
            "Orientation" 1,
            "PixelWidth" 4288,
            "ColorModel" "RGB",
            "{Exif}"
            {"FlashPixVersion" [1 0],
             "PixelXDimension" 4288,
             "ExposureBiasValue" 0,
             "PixelYDimension" 2848,
             "SceneCaptureType" 0,
             "FocalLength" 105,
             "LightSource" 9,
             "DateTimeDigitized" "2009:10:09 14:09:20",
             "ExposureTime" 0.005,
             "SensingMethod" 2,
             "Flash" 0,
             "ColorSpace" 1,
             "ExposureProgram" 3,
             "MaxApertureValue" 5,
             "ISOSpeedRatings" [400],
             "DateTimeOriginal" "2009:10:09 14:09:20",
             "ComponentsConfiguration" [1 2 3 0],
             "MeteringMode" 5,
             "ExifVersion" [2 2],
             "FNumber" 5.6},
            "{TIFF}"
            {"Software" "QuickTime 7.6.3",
             "Orientation" 1,
             "Make" "NIKON CORPORATION",
             "Model" "NIKON D90",
             "XResolution" 72,
             "PhotometricInterpretation" 2,
             "YResolution" 72,
             "DateTime" "2009:10:18 16:49:28",
             "ResolutionUnit" 2},
            "{JFIF}"
            {"XDensity" 72,
             "YDensity" 72,
             "JFIFVersion" [1 0 1],
             "DensityUnit" 1},
            "DPIHeight" 72,
            "PixelHeight" 2848,
            "Depth" 8,
            "ProfileName" "Adobe RGB (1998)"},
           :upload-start-at 1532159347286,
           :upload-id "4",
           :width 4288,
           :public-url
           "https://s3-us-west-2.amazonaws.com/nalopastures/images/b84e8479-475c-4727-a4a4-b77aa9980897-l0-001",
           :s3-bucket "nalopastures",
           :upload-success-at 1532159350028,
           :mime "image/jpeg",
           :size 2995977,
           :source-url
           "file:///Users/zk/Library/Developer/CoreSimulator/Devices/F95E235C-1F68-4645-B343-A9263EB961F7/data/Media/DCIM/100APPLE/IMG_0002.JPG",
           :s3-region "us-west-2",
           :filename "IMG_0002.JPG",
           :creation-date "1255122560",
           :s3-key "images/b84e8479-475c-4727-a4a4-b77aa9980897-l0-001",
           :local-identifier
           "B84E8479-475C-4727-A4A4-B77AA9980897/L0/001",
           :crop-rect nil,
           :height 2848,
           :data nil}},
         :start-ts 1532159232000,
         :debit-credit -785,
         :modified-at 1532159342390,
         :duration-ms 3600000,
         :syncing-at nil,
         :id "note/c914561efdba4749afc8045e7ffe3789",
         :body "The quick brown fox jumps over the lazy dog.",
         :synced-at 1532159350079}},
       :notes-order ["note/c914561efdba4749afc8045e7ffe3789"]},
      :updated-at 1532372116992}
     {:key ".!app",
      :value
      {:features
       {:id->feature
        {"feat/mango-tree-5"
         {:id "feat/mango-tree-5",
          :title "Mango Tree 5",
          :shape-type :point,
          :coord {:lng -157.7282694, :lat 21.3396566}},
         "feat/unknown-tree-11"
         {:id "feat/unknown-tree-11",
          :title "Unknown Tree 11",
          :shape-type :point,
          :coord {:lng -157.7281769, :lat 21.3403536}},
         "feat/house-avocado-tree-2"
         {:id "feat/house-avocado-tree-2",
          :title "House Avocado Tree 2",
          :shape-type :point,
          :coord {:lng -157.7284196, :lat 21.339814}},
         "feat/loafing-shed-7"
         {:id "feat/loafing-shed-7",
          :title "Loafing Shed 7",
          :shape-type :polygon,
          :coords
          [{:lng -157.7269793, :lat 21.3392194}
           {:lng -157.7269994, :lat 21.3391494}
           {:lng -157.726943, :lat 21.3391369}
           {:lng -157.726927, :lat 21.3392056}
           {:lng -157.7269793, :lat 21.3392194}],
          :bounds
          {:east -157.726927,
           :west -157.7269994,
           :north 21.3392194,
           :south 21.3391369}},
         "feat/unknown-tree-8"
         {:id "feat/unknown-tree-8",
          :title "Unknown Tree 8",
          :shape-type :point,
          :coord {:lng -157.7277222, :lat 21.34018}},
         "feat/mango-tree-2"
         {:id "feat/mango-tree-2",
          :title "Mango Tree 2",
          :shape-type :point,
          :coord {:lng -157.7283284, :lat 21.339443}},
         "feat/house-avocado-tree-5"
         {:id "feat/house-avocado-tree-5",
          :title "House Avocado Tree 5",
          :shape-type :point,
          :coord {:lng -157.7284317, :lat 21.3397827}},
         "feat/unknown-tree-1"
         {:id "feat/unknown-tree-1",
          :title "Unknown Tree 1",
          :shape-type :point,
          :coord {:lng -157.72848, :lat 21.3397865}},
         "feat/jump-box"
         {:id "feat/jump-box",
          :title "Jump Box",
          :shape-type :polygon,
          :coords
          [{:lng -157.7276766, :lat 21.3398402}
           {:lng -157.7276941, :lat 21.339789}
           {:lng -157.7276056, :lat 21.339764}
           {:lng -157.7275881, :lat 21.3398165}
           {:lng -157.7276766, :lat 21.3398402}],
          :bounds
          {:east -157.7275881,
           :west -157.7276941,
           :north 21.3398402,
           :south 21.339764}},
         "feat/palm-tree-4"
         {:id "feat/palm-tree-4",
          :title "Palm Tree 4",
          :shape-type :point,
          :coord {:lng -157.7285671, :lat 21.3401975}},
         "feat/pasture-2-avocado-tree-1"
         {:id "feat/pasture-2-avocado-tree-1",
          :title "Pasture 2 Avocado Tree 1",
          :shape-type :polygon,
          :coords
          [{:lng -157.7282855, :lat 21.3404773}
           {:lng -157.7282955, :lat 21.340451}
           {:lng -157.7282801, :lat 21.3404286}
           {:lng -157.7282466, :lat 21.3404173}
           {:lng -157.7282251, :lat 21.3404261}
           {:lng -157.7282198, :lat 21.3404498}
           {:lng -157.7282225, :lat 21.3404773}
           {:lng -157.728254, :lat 21.3404935}
           {:lng -157.7282855, :lat 21.3404773}],
          :bounds
          {:east -157.7282198,
           :west -157.7282955,
           :north 21.3404935,
           :south 21.3404173}},
         "feat/palm-tree-1"
         {:id "feat/palm-tree-1",
          :title "Palm Tree 1",
          :shape-type :point,
          :coord {:lng -157.7284129, :lat 21.3401725}},
         "feat/unknown-tree-13"
         {:id "feat/unknown-tree-13",
          :title "Unknown Tree 13",
          :shape-type :point,
          :coord {:lng -157.7285926, :lat 21.3398402}},
         "feat/mango-tree-7"
         {:id "feat/mango-tree-7",
          :title "Mango Tree 7",
          :shape-type :point,
          :coord {:lng -157.7282359, :lat 21.3397752}},
         "feat/house-lychee-tree-1"
         {:id "feat/house-lychee-tree-1",
          :title "House Lychee Tree 1",
          :shape-type :polygon,
          :coords
          [{:lng -157.7283217, :lat 21.3399814}
           {:lng -157.7283418, :lat 21.3399582}
           {:lng -157.7283311, :lat 21.3399214}
           {:lng -157.7283002, :lat 21.3399089}
           {:lng -157.7282815, :lat 21.3399201}
           {:lng -157.7282727, :lat 21.3399414}
           {:lng -157.7282721, :lat 21.3399714}
           {:lng -157.7282929, :lat 21.3399932}
           {:lng -157.7283217, :lat 21.3399814}],
          :bounds
          {:east -157.7282721,
           :west -157.7283418,
           :north 21.3399932,
           :south 21.3399089}},
         "feat/palm-tree-5"
         {:id "feat/palm-tree-5",
          :title "Palm Tree 5",
          :shape-type :point,
          :coord {:lng -157.728598, :lat 21.3402037}},
         "feat/loafing-shed-6"
         {:id "feat/loafing-shed-6",
          :title "Loafing Shed 6",
          :shape-type :polygon,
          :coords
          [{:lng -157.7272488, :lat 21.3402262}
           {:lng -157.7272636, :lat 21.3401837}
           {:lng -157.7271885, :lat 21.3401612}
           {:lng -157.7271724, :lat 21.3402062}
           {:lng -157.7272488, :lat 21.3402262}],
          :bounds
          {:east -157.7271724,
           :west -157.7272636,
           :north 21.3402262,
           :south 21.3401612}},
         "feat/pasture-5"
         {:id "feat/pasture-5",
          :title "Pasture 5",
          :shape-type :polygon,
          :coords
          [{:lng -157.7274165, :lat 21.3406359}
           {:lng -157.7275009, :lat 21.3403611}
           {:lng -157.7274808, :lat 21.3403274}
           {:lng -157.7271388, :lat 21.3402374}
           {:lng -157.7271, :lat 21.3402574}
           {:lng -157.7270758, :lat 21.3403436}
           {:lng -157.7270865, :lat 21.3403748}
           {:lng -157.7273306, :lat 21.3406159}
           {:lng -157.727387, :lat 21.3406459}
           {:lng -157.7274165, :lat 21.3406359}],
          :bounds
          {:east -157.7270758,
           :west -157.7275009,
           :north 21.3406459,
           :south 21.3402374}},
         "feat/pasture-3"
         {:id "feat/pasture-3",
          :title "Pasture 3",
          :shape-type :polygon,
          :coords
          [{:lng -157.7278872, :lat 21.3407508}
           {:lng -157.7279703, :lat 21.3404835}
           {:lng -157.7279489, :lat 21.3404523}
           {:lng -157.7275881, :lat 21.3403511}
           {:lng -157.7275439, :lat 21.3403723}
           {:lng -157.7274634, :lat 21.3406509}
           {:lng -157.7274808, :lat 21.3406809}
           {:lng -157.727847, :lat 21.3407846}
           {:lng -157.7278872, :lat 21.3407508}],
          :bounds
          {:east -157.7274634,
           :west -157.7279703,
           :north 21.3407846,
           :south 21.3403511}},
         "feat/pasture-2"
         {:id "feat/pasture-2",
          :title "Pasture 2",
          :shape-type :polygon,
          :coords
          [{:lng -157.7285309, :lat 21.3405285}
           {:lng -157.7285618, :lat 21.3403586}
           {:lng -157.7285416, :lat 21.3403274}
           {:lng -157.7281299, :lat 21.3402212}
           {:lng -157.7280883, :lat 21.3402399}
           {:lng -157.7280414, :lat 21.3404011}
           {:lng -157.7280615, :lat 21.3404335}
           {:lng -157.7285041, :lat 21.3405497}
           {:lng -157.7285309, :lat 21.3405285}],
          :bounds
          {:east -157.7280414,
           :west -157.7285618,
           :north 21.3405497,
           :south 21.3402212}},
         "feat/pasture-2-mango-tree-1"
         {:id "feat/pasture-2-mango-tree-1",
          :title "Pasture 2 Mango Tree 1",
          :shape-type :point,
          :coord {:lng -157.7284813, :lat 21.3404335}},
         "feat/house-avocado-tree-4"
         {:id "feat/house-avocado-tree-4",
          :title "House Avocado Tree 4",
          :shape-type :point,
          :coord {:lng -157.7283901, :lat 21.3397702}},
         "feat/palm-tree-6"
         {:id "feat/palm-tree-6",
          :title "Palm Tree 6",
          :shape-type :point,
          :coord {:lng -157.7283847, :lat 21.3405023}},
         "feat/pasture-4"
         {:id "feat/pasture-4",
          :title "Pasture 4",
          :shape-type :polygon,
          :coords
          [{:lng -157.7279985, :lat 21.3403898}
           {:lng -157.7280454, :lat 21.3402274}
           {:lng -157.7280253, :lat 21.3401975}
           {:lng -157.7274393, :lat 21.3400488}
           {:lng -157.727403, :lat 21.3400663}
           {:lng -157.7273548, :lat 21.3402237}
           {:lng -157.7273762, :lat 21.3402574}
           {:lng -157.7279583, :lat 21.3404073}
           {:lng -157.7279985, :lat 21.3403898}],
          :bounds
          {:east -157.7273548,
           :west -157.7280454,
           :north 21.3404073,
           :south 21.3400488}},
         "feat/mango-tree-1"
         {:id "feat/mango-tree-1",
          :title "Mango Tree 1",
          :shape-type :polygon,
          :coords
          [{:lng -157.7284786, :lat 21.3395092}
           {:lng -157.7285048, :lat 21.339478}
           {:lng -157.7285095, :lat 21.3394467}
           {:lng -157.7285028, :lat 21.3394017}
           {:lng -157.7284538, :lat 21.3393805}
           {:lng -157.7284102, :lat 21.3393893}
           {:lng -157.728384, :lat 21.3394224}
           {:lng -157.7283793, :lat 21.3394705}
           {:lng -157.7284062, :lat 21.3395004}
           {:lng -157.7284397, :lat 21.3395123}
           {:lng -157.7284786, :lat 21.3395092}],
          :bounds
          {:east -157.7283793,
           :west -157.7285095,
           :north 21.3395123,
           :south 21.3393805}},
         "feat/mango-tree-6"
         {:id "feat/mango-tree-6",
          :title "Mango Tree 6",
          :shape-type :point,
          :coord {:lng -157.7282507, :lat 21.3397116}},
         "feat/unknown-tree-7"
         {:id "feat/unknown-tree-7",
          :title "Unknown Tree 7",
          :shape-type :point,
          :coord {:lng -157.7273923, :lat 21.340461}},
         "feat/house-breadfruit-tree-1"
         {:id "feat/house-breadfruit-tree-1",
          :title "House Breadfruit Tree 1",
          :shape-type :polygon,
          :coords
          [{:lng -157.7285229, :lat 21.340075}
           {:lng -157.7285269, :lat 21.3400338}
           {:lng -157.7284813, :lat 21.3400226}
           {:lng -157.7284773, :lat 21.3400638}
           {:lng -157.7285229, :lat 21.340075}],
          :bounds
          {:east -157.7284773,
           :west -157.7285269,
           :north 21.340075,
           :south 21.3400226}},
         "feat/unknown-tree-12"
         {:id "feat/unknown-tree-12",
          :title "Unknown Tree 12",
          :shape-type :point,
          :coord {:lng -157.7279234, :lat 21.3407896}},
         "feat/lime-tree-1"
         {:id "feat/lime-tree-1",
          :title "Lime Tree 1",
          :shape-type :point,
          :coord {:lng -157.7284639, :lat 21.3400301}},
         "feat/hunt-field-fence"
         {:id "feat/hunt-field-fence",
          :title "Hunt Field Fence",
          :shape-type :polyline,
          :coords
          [{:lng -157.7282158, :lat 21.3396791}
           {:lng -157.7282962, :lat 21.3394092}
           {:lng -157.7273655, :lat 21.3391694}],
          :bounds
          {:east -157.7273655,
           :west -157.7282962,
           :north 21.3396791,
           :south 21.3391694}},
         "feat/loafing-shed-3"
         {:id "feat/loafing-shed-3",
          :title "Loafing Shed 3",
          :shape-type :polygon,
          :coords
          [{:lng -157.7275532, :lat 21.3404735}
           {:lng -157.727576, :lat 21.3404086}
           {:lng -157.7275291, :lat 21.3403948}
           {:lng -157.7275103, :lat 21.340461}
           {:lng -157.7275532, :lat 21.3404735}],
          :bounds
          {:east -157.7275103,
           :west -157.727576,
           :north 21.3404735,
           :south 21.3403948}},
         "feat/property-line"
         {:id "feat/property-line",
          :title "Property Line",
          :shape-type :polygon,
          :coords
          [{:lng -157.7285028, :lat 21.3410381}
           {:lng -157.7286905, :lat 21.3399938}
           {:lng -157.7286529, :lat 21.339799}
           {:lng -157.728712, :lat 21.3394642}
           {:lng -157.7269471, :lat 21.3390145}
           {:lng -157.7265179, :lat 21.3403736}
           {:lng -157.7270061, :lat 21.3403936}
           {:lng -157.7272904, :lat 21.3406784}
           {:lng -157.7285028, :lat 21.3410381}],
          :bounds
          {:east -157.7265179,
           :west -157.728712,
           :north 21.3410381,
           :south 21.3390145}},
         "feat/house-avocado-tree-3"
         {:id "feat/house-avocado-tree-3",
          :title "House Avocado Tree 3",
          :shape-type :point,
          :coord {:lng -157.728378, :lat 21.339804}},
         "feat/mango-tree-4"
         {:id "feat/mango-tree-4",
          :title "Mango Tree 4",
          :shape-type :point,
          :coord {:lng -157.7282868, :lat 21.3395904}},
         "feat/mango-tree-3"
         {:id "feat/mango-tree-3",
          :title "Mango Tree 3",
          :shape-type :point,
          :coord {:lng -157.7283096, :lat 21.3395205}},
         "feat/unknown-tree-9"
         {:id "feat/unknown-tree-9",
          :title "Unknown Tree 9",
          :shape-type :point,
          :coord {:lng -157.7276967, :lat 21.3406771}},
         "feat/palm-tree-2"
         {:id "feat/palm-tree-2",
          :title "Palm Tree 2",
          :shape-type :point,
          :coord {:lng -157.7284813, :lat 21.3401825}},
         "feat/palm-tree-3"
         {:id "feat/palm-tree-3",
          :title "Palm Tree 3",
          :shape-type :point,
          :coord {:lng -157.7285269, :lat 21.3401887}},
         "feat/unknown-tree-2"
         {:id "feat/unknown-tree-2",
          :title "Unknown Tree 2",
          :shape-type :point,
          :coord {:lng -157.7285296, :lat 21.3397865}},
         "feat/unknown-tree-6"
         {:id "feat/unknown-tree-6",
          :title "Unknown Tree 6",
          :shape-type :point,
          :coord {:lng -157.7267043, :lat 21.3402112}},
         "feat/pasture-7"
         {:id "feat/pasture-7",
          :title "Pasture 7",
          :shape-type :polygon,
          :coords
          [{:lng -157.7270919, :lat 21.3398827}
           {:lng -157.7272944, :lat 21.3391794}
           {:lng -157.7272716, :lat 21.3391457}
           {:lng -157.7270007, :lat 21.3390782}
           {:lng -157.7269605, :lat 21.3390957}
           {:lng -157.726762, :lat 21.3398002}
           {:lng -157.7267794, :lat 21.3398327}
           {:lng -157.7270544, :lat 21.3399052}
           {:lng -157.7270919, :lat 21.3398827}],
          :bounds
          {:east -157.726762,
           :west -157.7272944,
           :north 21.3399052,
           :south 21.3390782}},
         "feat/loafing-shed-1"
         {:id "feat/loafing-shed-1",
          :title "Loafing Shed 1",
          :shape-type :polygon,
          :coords
          [{:lng -157.7280388, :lat 21.3405285}
           {:lng -157.7279945, :lat 21.3405147}
           {:lng -157.7279797, :lat 21.3405834}
           {:lng -157.7280199, :lat 21.3405946}
           {:lng -157.7280388, :lat 21.3405285}],
          :bounds
          {:east -157.7279797,
           :west -157.7280388,
           :north 21.3405946,
           :south 21.3405147}},
         "feat/unknown-tree-10"
         {:id "feat/unknown-tree-10",
          :title "Unknown Tree 10",
          :shape-type :point,
          :coord {:lng -157.7281956, :lat 21.340827}},
         "feat/house-avocado-tree-1"
         {:id "feat/house-avocado-tree-1",
          :title "House Avocado Tree 1",
          :shape-type :point,
          :coord {:lng -157.7284719, :lat 21.339814}},
         "feat/pasture-1"
         {:id "feat/pasture-1",
          :title "Pasture 1",
          :shape-type :polygon,
          :coords
          [{:lng -157.7284585, :lat 21.3409182}
           {:lng -157.7285148, :lat 21.3406259}
           {:lng -157.728492, :lat 21.3405897}
           {:lng -157.7280535, :lat 21.3404735}
           {:lng -157.7280132, :lat 21.3404935}
           {:lng -157.7279328, :lat 21.3407533}
           {:lng -157.7279328, :lat 21.3408058}
           {:lng -157.7284196, :lat 21.340942}
           {:lng -157.7284585, :lat 21.3409182}],
          :bounds
          {:east -157.7279328,
           :west -157.7285148,
           :north 21.340942,
           :south 21.3404735}},
         "feat/unknown-tree-3"
         {:id "feat/unknown-tree-3",
          :title "Unknown Tree 3",
          :shape-type :point,
          :coord {:lng -157.7270503, :lat 21.3397665}},
         "feat/unknown-tree-5"
         {:id "feat/unknown-tree-5",
          :title "Unknown Tree 5",
          :shape-type :point,
          :coord {:lng -157.7267647, :lat 21.3400076}},
         "feat/unknown-tree-4"
         {:id "feat/unknown-tree-4",
          :title "Unknown Tree 4",
          :shape-type :point,
          :coord {:lng -157.7268679, :lat 21.3397103}},
         "feat/loafing-shed-4"
         {:id "feat/loafing-shed-4",
          :title "Loafing Shed 4",
          :shape-type :polygon,
          :coords
          [{:lng -157.7274647, :lat 21.3402824}
           {:lng -157.7274795, :lat 21.3402299}
           {:lng -157.7274084, :lat 21.3402112}
           {:lng -157.7273923, :lat 21.3402637}
           {:lng -157.7274647, :lat 21.3402824}],
          :bounds
          {:east -157.7273923,
           :west -157.7274795,
           :north 21.3402824,
           :south 21.3402112}},
         "feat/hunt-field-base"
         {:id "feat/hunt-field-base",
          :title "Hunt Field Base",
          :shape-type :polygon,
          :coords
          [{:lng -157.7280656, :lat 21.3401412}
           {:lng -157.7282962, :lat 21.3394092}
           {:lng -157.7274191, :lat 21.3391944}
           {:lng -157.7272072, :lat 21.3399214}
           {:lng -157.7280656, :lat 21.3401412}],
          :bounds
          {:east -157.7272072,
           :west -157.7282962,
           :north 21.3401412,
           :south 21.3391944}},
         "feat/pasture-6"
         {:id "feat/pasture-6",
          :title "Pasture 6",
          :shape-type :polygon,
          :coords
          [{:lng -157.7273145, :lat 21.3402112}
           {:lng -157.7273601, :lat 21.3400551}
           {:lng -157.7273387, :lat 21.3400226}
           {:lng -157.7267714, :lat 21.3398727}
           {:lng -157.7267352, :lat 21.3398914}
           {:lng -157.7266118, :lat 21.3402936}
           {:lng -157.7266413, :lat 21.3403236}
           {:lng -157.7268049, :lat 21.3403111}
           {:lng -157.7269994, :lat 21.3403286}
           {:lng -157.7270356, :lat 21.3403074}
           {:lng -157.7270691, :lat 21.3402075}
           {:lng -157.7271093, :lat 21.3401887}
           {:lng -157.7272783, :lat 21.3402337}
           {:lng -157.7273145, :lat 21.3402112}],
          :bounds
          {:east -157.7266118,
           :west -157.7273601,
           :north 21.3403286,
           :south 21.3398727}}},
        :order
        ("feat/property-line"
         "feat/pasture-1"
         "feat/loafing-shed-1"
         "feat/pasture-2"
         "feat/pasture-3"
         "feat/loafing-shed-3"
         "feat/pasture-4"
         "feat/loafing-shed-4"
         "feat/pasture-5"
         "feat/pasture-6"
         "feat/loafing-shed-6"
         "feat/pasture-7"
         "feat/loafing-shed-7"
         "feat/mango-tree-1"
         "feat/pasture-2-avocado-tree-1"
         "feat/house-breadfruit-tree-1"
         "feat/house-lychee-tree-1"
         "feat/mango-tree-2"
         "feat/mango-tree-3"
         "feat/mango-tree-4"
         "feat/mango-tree-5"
         "feat/mango-tree-6"
         "feat/mango-tree-7"
         "feat/pasture-2-mango-tree-1"
         "feat/lime-tree-1"
         "feat/house-avocado-tree-1"
         "feat/house-avocado-tree-2"
         "feat/house-avocado-tree-3"
         "feat/house-avocado-tree-4"
         "feat/house-avocado-tree-5"
         "feat/hunt-field-base"
         "feat/hunt-field-fence"
         "feat/jump-box"
         "feat/palm-tree-1"
         "feat/palm-tree-2"
         "feat/palm-tree-3"
         "feat/palm-tree-4"
         "feat/palm-tree-5"
         "feat/palm-tree-6"
         "feat/unknown-tree-1"
         "feat/unknown-tree-2"
         "feat/unknown-tree-3"
         "feat/unknown-tree-4"
         "feat/unknown-tree-5"
         "feat/unknown-tree-6"
         "feat/unknown-tree-7"
         "feat/unknown-tree-8"
         "feat/unknown-tree-9"
         "feat/unknown-tree-10"
         "feat/unknown-tree-11"
         "feat/unknown-tree-12"
         "feat/unknown-tree-13")},
       :layers
       {:id->layer
        {"layer/property-bounds"
         {:id "layer/property-bounds",
          :title "Property Bounds",
          :feature-ids ["feat/property-line"]},
         "layer/pastures"
         {:id "layer/pastures",
          :title "Pastures",
          :feature-ids
          ["feat/pasture-1"
           "feat/loafing-shed-1"
           "feat/pasture-2"
           "feat/pasture-3"
           "feat/loafing-shed-3"
           "feat/pasture-4"
           "feat/loafing-shed-4"
           "feat/pasture-5"
           "feat/pasture-6"
           "feat/loafing-shed-6"
           "feat/pasture-7"
           "feat/loafing-shed-7"]},
         "layer/fruit-trees"
         {:id "layer/fruit-trees",
          :title "Fruit Trees",
          :feature-ids
          ["feat/mango-tree-1"
           "feat/pasture-2-avocado-tree-1"
           "feat/house-breadfruit-tree-1"
           "feat/house-lychee-tree-1"
           "feat/mango-tree-2"
           "feat/mango-tree-3"
           "feat/mango-tree-4"
           "feat/mango-tree-5"
           "feat/mango-tree-6"
           "feat/mango-tree-7"
           "feat/pasture-2-mango-tree-1"
           "feat/lime-tree-1"
           "feat/house-avocado-tree-1"
           "feat/house-avocado-tree-2"
           "feat/house-avocado-tree-3"
           "feat/house-avocado-tree-4"
           "feat/house-avocado-tree-5"]},
         "layer/hunt-field"
         {:id "layer/hunt-field",
          :title "Hunt Field",
          :feature-ids
          ["feat/hunt-field-base"
           "feat/hunt-field-fence"
           "feat/jump-box"]},
         "layer/non-fruiting-trees"
         {:id "layer/non-fruiting-trees",
          :title "Non Fruiting Trees",
          :feature-ids
          ["feat/palm-tree-1"
           "feat/palm-tree-2"
           "feat/palm-tree-3"
           "feat/palm-tree-4"
           "feat/palm-tree-5"
           "feat/palm-tree-6"]},
         "layer/unknown-trees"
         {:id "layer/unknown-trees",
          :title "Unknown Trees",
          :feature-ids
          ["feat/unknown-tree-1"
           "feat/unknown-tree-2"
           "feat/unknown-tree-3"
           "feat/unknown-tree-4"
           "feat/unknown-tree-5"
           "feat/unknown-tree-6"
           "feat/unknown-tree-7"
           "feat/unknown-tree-8"
           "feat/unknown-tree-9"
           "feat/unknown-tree-10"
           "feat/unknown-tree-11"
           "feat/unknown-tree-12"
           "feat/unknown-tree-13"]}},
        :order
        ("layer/property-bounds"
         "layer/pastures"
         "layer/fruit-trees"
         "layer/hunt-field"
         "layer/non-fruiting-trees"
         "layer/unknown-trees")}},
      :updated-at 1532372116992}),
    :test-states
    [{:section "General",
      :title "Clean Slate",
      :state {:app {}, :notes {}}}
     {:section "Notes History",
      :title "Clean Slate",
      :state
      {:app {},
       :notes
       {:id->note
        {"note/c914561efdba4749afc8045e7ffe3789"
         {:images
          {"99D53A1F-FEEF-40E1-8BB3-7DD55A43C8B7/L0/001"
           {:modification-date "1441224147",
            :path
            "/Users/zk/Library/Developer/CoreSimulator/Devices/F95E235C-1F68-4645-B343-A9263EB961F7/data/Containers/Data/Application/AF473EE8-43AC-4EAA-8E7F-18DDB95AA9A3/tmp/react-native-image-crop-picker/E0907704-2A26-4CE9-91D6-21B8F62FA223.jpg",
            :upload-progress 100,
            :exif
            {"DPIWidth" 72,
             "{IPTC}"
             {"Byline" ["Nicolas Cornet"],
              "DigitalCreationDate" "20120808",
              "ObjectName" "Berunes",
              "DigitalCreationTime" "142949",
              "DateCreated" "20120808",
              "TimeCreated" "142949",
              "CopyrightNotice" "Nicolas Cornet"},
             "Orientation" 1,
             "{GPS}"
             {"LongitudeRef" "W",
              "Longitude" 14.538611666666666,
              "SpeedRef" "K",
              "GPSVersion" [2 3 0 0],
              "Altitude" 182.33333333333334,
              "Speed" 0.8999988197665498,
              "LatitudeRef" "N",
              "MapDatum" "WGS-84",
              "ImgDirectionRef" "T",
              "Latitude" 64.752895,
              "ImgDirection" 116.36669970267592},
             "PixelWidth" 1668,
             "ColorModel" "RGB",
             "{Exif}"
             {"ExposureMode" 1,
              "FlashPixVersion" [1 0],
              "PixelXDimension" 1668,
              "SubjectDistance" 1.33,
              "ExposureBiasValue" 0,
              "PixelYDimension" 2500,
              "SceneCaptureType" 0,
              "SubsecTimeDigitized" "8",
              "Sharpness" 0,
              "Saturation" 0,
              "FocalLength" 32,
              "LightSource" 0,
              "Contrast" 0,
              "FocalLenIn35mmFilm" 32,
              "DateTimeDigitized" "2012:08:08 14:29:49",
              "ExposureTime" 0.8,
              "SensingMethod" 2,
              "Flash" 16,
              "ColorSpace" 1,
              "ExposureProgram" 1,
              "MaxApertureValue" 4,
              "DigitalZoomRatio" 1,
              "FocalPlaneXResolution" 204.840206185567,
              "FileSource" 3,
              "SubjectDistRange" 0,
              "ISOSpeedRatings" [50],
              "FocalPlaneResolutionUnit" 4,
              "DateTimeOriginal" "2012:08:08 14:29:49",
              "WhiteBalance" 0,
              "GainControl" 0,
              "ApertureValue" 8.918862690707352,
              "CustomRendered" 0,
              "SubsecTimeOriginal" "8",
              "FocalPlaneYResolution" 204.840206185567,
              "ShutterSpeedValue" 0.32192799761975605,
              "ComponentsConfiguration" [1 2 3 0],
              "MeteringMode" 5,
              "ExifVersion" [2 3],
              "FNumber" 22},
             "{TIFF}"
             {"Software" "Aperture 3.4.5",
              "Orientation" 1,
              "Artist" "Nicolas Cornet",
              "Make" "NIKON CORPORATION",
              "Model" "NIKON D800E",
              "XResolution" 72,
              "YResolution" 72,
              "Copyright" "Nicolas Cornet",
              "DateTime" "2012:08:08 14:29:49",
              "ResolutionUnit" 2},
             "{JFIF}"
             {"XDensity" 72,
              "YDensity" 72,
              "JFIFVersion" [1 0 1],
              "DensityUnit" 1},
             "DPIHeight" 72,
             "PixelHeight" 2500,
             "{ExifAux}"
             {"LensModel" "16.0-35.0 mm f/4.0",
              "SerialNumber" "6001440",
              "ImageNumber" 10075,
              "LensInfo" [16 35 4 4],
              "LensID" 163},
             "Depth" 8,
             "ProfileName" "sRGB IEC61966-2.1"},
            :upload-start-at 1532159342404,
            :upload-id "2",
            :width 1668,
            :public-url
            "https://s3-us-west-2.amazonaws.com/nalopastures/images/99d53a1f-feef-40e1-8bb3-7dd55a43c8b7-l0-001",
            :s3-bucket "nalopastures",
            :upload-success-at 1532159345140,
            :mime "image/jpeg",
            :size 1253001,
            :source-url
            "file:///Users/zk/Library/Developer/CoreSimulator/Devices/F95E235C-1F68-4645-B343-A9263EB961F7/data/Media/DCIM/100APPLE/IMG_0004.JPG",
            :s3-region "us-west-2",
            :filename "IMG_0004.JPG",
            :creation-date "1344461390",
            :s3-key "images/99d53a1f-feef-40e1-8bb3-7dd55a43c8b7-l0-001",
            :local-identifier
            "99D53A1F-FEEF-40E1-8BB3-7DD55A43C8B7/L0/001",
            :crop-rect nil,
            :height 2500,
            :data nil},
           "9F983DBA-EC35-42B8-8773-B597CF782EDD/L0/001"
           {:modification-date "1441224147",
            :path
            "/Users/zk/Library/Developer/CoreSimulator/Devices/F95E235C-1F68-4645-B343-A9263EB961F7/data/Containers/Data/Application/AF473EE8-43AC-4EAA-8E7F-18DDB95AA9A3/tmp/react-native-image-crop-picker/7ED71548-F1E8-401C-88DB-517E99BA90A3.jpg",
            :upload-progress 100,
            :exif
            {"DPIWidth" 72,
             "{IPTC}"
             {"DigitalCreationTime" "115211",
              "DigitalCreationDate" "20120808",
              "Province/State" "Northeast",
              "Byline" ["Nicolas Cornet"],
              "ObjectName" "Godafoss",
              "TimeCreated" "115211",
              "Country/PrimaryLocationName" "Iceland",
              "DateCreated" "20120808",
              "SubLocation" "Godafoss",
              "City" "Ljósavatn",
              "CopyrightNotice" "Nicolas Cornet"},
             "Orientation" 1,
             "{GPS}"
             {"LongitudeRef" "W",
              "Longitude" 17.548928333333333,
              "SpeedRef" "K",
              "GPSVersion" [2 3 0 0],
              "Altitude" 103,
              "Speed" 1.6,
              "LatitudeRef" "N",
              "MapDatum" "WGS-84",
              "ImgDirectionRef" "T",
              "Latitude" 65.682895,
              "ImgDirection" 302.4},
             "PixelWidth" 3000,
             "ColorModel" "RGB",
             "{Exif}"
             {"ExposureMode" 1,
              "FlashPixVersion" [1 0],
              "PixelXDimension" 3000,
              "SubjectDistance" 2.99,
              "ExposureBiasValue" 0,
              "PixelYDimension" 2002,
              "SceneCaptureType" 0,
              "SubsecTimeDigitized" "9",
              "Sharpness" 0,
              "Saturation" 0,
              "FocalLength" 24,
              "LightSource" 0,
              "Contrast" 0,
              "FocalLenIn35mmFilm" 24,
              "DateTimeDigitized" "2012:08:08 11:52:11",
              "ExposureTime" 4,
              "SensingMethod" 2,
              "Flash" 16,
              "ColorSpace" 1,
              "ExposureProgram" 1,
              "MaxApertureValue" 4,
              "DigitalZoomRatio" 1,
              "FocalPlaneXResolution" 204.840206185567,
              "FileSource" 3,
              "SubjectDistRange" 0,
              "ISOSpeedRatings" [200],
              "FocalPlaneResolutionUnit" 4,
              "DateTimeOriginal" "2012:08:08 11:52:11",
              "WhiteBalance" 0,
              "GainControl" 0,
              "ApertureValue" 6.643855776306108,
              "CustomRendered" 0,
              "SubsecTimeOriginal" "9",
              "FocalPlaneYResolution" 204.840206185567,
              "ShutterSpeedValue" -2,
              "ComponentsConfiguration" [1 2 3 0],
              "MeteringMode" 5,
              "ExifVersion" [2 3],
              "FNumber" 10},
             "{TIFF}"
             {"Software" "Aperture 3.4.5",
              "Orientation" 1,
              "Artist" "Nicolas Cornet",
              "Make" "NIKON CORPORATION",
              "Model" "NIKON D800E",
              "XResolution" 72,
              "YResolution" 72,
              "Copyright" "Nicolas Cornet",
              "DateTime" "2012:08:08 11:52:11",
              "ResolutionUnit" 2},
             "DPIHeight" 72,
             "PixelHeight" 2002,
             "{ExifAux}"
             {"LensModel" "24.0-120.0 mm f/4.0",
              "SerialNumber" "6001440",
              "ImageNumber" 8458,
              "LensInfo" [24 120 4 4],
              "LensID" 170},
             "Depth" 8,
             "ProfileName" "sRGB IEC61966-2.1"},
            :upload-start-at 1532159345145,
            :upload-id "3",
            :width 3000,
            :public-url
            "https://s3-us-west-2.amazonaws.com/nalopastures/images/9f983dba-ec35-42b8-8773-b597cf782edd-l0-001",
            :s3-bucket "nalopastures",
            :upload-success-at 1532159347280,
            :mime "image/jpeg",
            :size 2475488,
            :source-url
            "file:///Users/zk/Library/Developer/CoreSimulator/Devices/F95E235C-1F68-4645-B343-A9263EB961F7/data/Media/DCIM/100APPLE/IMG_0003.JPG",
            :s3-region "us-west-2",
            :filename "IMG_0003.JPG",
            :creation-date "1344451932",
            :s3-key "images/9f983dba-ec35-42b8-8773-b597cf782edd-l0-001",
            :local-identifier
            "9F983DBA-EC35-42B8-8773-B597CF782EDD/L0/001",
            :crop-rect nil,
            :height 2002,
            :data nil},
           "B84E8479-475C-4727-A4A4-B77AA9980897/L0/001"
           {:modification-date "1441224147",
            :path
            "/Users/zk/Library/Developer/CoreSimulator/Devices/F95E235C-1F68-4645-B343-A9263EB961F7/data/Containers/Data/Application/AF473EE8-43AC-4EAA-8E7F-18DDB95AA9A3/tmp/react-native-image-crop-picker/E031E630-C6D1-470B-BA02-707BCB8B1413.jpg",
            :upload-progress 100,
            :exif
            {"DPIWidth" 72,
             "{IPTC}"
             {"DigitalCreationTime" "140920",
              "DigitalCreationDate" "20091009",
              "ObjectName" "DSC_0010",
              "DateCreated" "20091009",
              "TimeCreated" "140920"},
             "Orientation" 1,
             "PixelWidth" 4288,
             "ColorModel" "RGB",
             "{Exif}"
             {"FlashPixVersion" [1 0],
              "PixelXDimension" 4288,
              "ExposureBiasValue" 0,
              "PixelYDimension" 2848,
              "SceneCaptureType" 0,
              "FocalLength" 105,
              "LightSource" 9,
              "DateTimeDigitized" "2009:10:09 14:09:20",
              "ExposureTime" 0.005,
              "SensingMethod" 2,
              "Flash" 0,
              "ColorSpace" 1,
              "ExposureProgram" 3,
              "MaxApertureValue" 5,
              "ISOSpeedRatings" [400],
              "DateTimeOriginal" "2009:10:09 14:09:20",
              "ComponentsConfiguration" [1 2 3 0],
              "MeteringMode" 5,
              "ExifVersion" [2 2],
              "FNumber" 5.6},
             "{TIFF}"
             {"Software" "QuickTime 7.6.3",
              "Orientation" 1,
              "Make" "NIKON CORPORATION",
              "Model" "NIKON D90",
              "XResolution" 72,
              "PhotometricInterpretation" 2,
              "YResolution" 72,
              "DateTime" "2009:10:18 16:49:28",
              "ResolutionUnit" 2},
             "{JFIF}"
             {"XDensity" 72,
              "YDensity" 72,
              "JFIFVersion" [1 0 1],
              "DensityUnit" 1},
             "DPIHeight" 72,
             "PixelHeight" 2848,
             "Depth" 8,
             "ProfileName" "Adobe RGB (1998)"},
            :upload-start-at 1532159347286,
            :upload-id "4",
            :width 4288,
            :public-url
            "https://s3-us-west-2.amazonaws.com/nalopastures/images/b84e8479-475c-4727-a4a4-b77aa9980897-l0-001",
            :s3-bucket "nalopastures",
            :upload-success-at 1532159350028,
            :mime "image/jpeg",
            :size 2995977,
            :source-url
            "file:///Users/zk/Library/Developer/CoreSimulator/Devices/F95E235C-1F68-4645-B343-A9263EB961F7/data/Media/DCIM/100APPLE/IMG_0002.JPG",
            :s3-region "us-west-2",
            :filename "IMG_0002.JPG",
            :creation-date "1255122560",
            :s3-key "images/b84e8479-475c-4727-a4a4-b77aa9980897-l0-001",
            :local-identifier
            "B84E8479-475C-4727-A4A4-B77AA9980897/L0/001",
            :crop-rect nil,
            :height 2848,
            :data nil}},
          :start-ts 1532159232000,
          :debit-credit -785,
          :modified-at 1532159342390,
          :duration-ms 3600000,
          :syncing-at nil,
          :id "note/c914561efdba4749afc8045e7ffe3789",
          :body "The quick brown fox jumps over the lazy dog.",
          :synced-at 1532159350079}},
        :notes-order ["note/c914561efdba4749afc8045e7ffe3789"]}}}
     {:section "New Note",
      :title "Clean Slate",
      :state {:notes {:initial {:slider {:visible? true}}}}}
     {:section "New Note",
      :title "Populated",
      :state
      {:notes
       {:initial
        {:slider {:visible? true},
         :new-note
         {:ui
          {:body "The quick brown fox jumps over the lazy dog.",
           :start-ts 1532372016713,
           :duration-ms 5400000,
           :debit-credit -120}}}}}}
     {:section "New Note",
      :title "Debit / Credit",
      :state
      {:app {},
       :notes
       {:initial {:slider {:visible? true}},
        :initial-ui
        {:routes [{:view :default} {:view :pick-debit-credit}],
         :default-view
         {:body "The quick brown fox jumps over the lazy dog.",
          :start-ts 1532372016713,
          :duration-ms 5400000}}}}}]}
  )
