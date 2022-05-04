import Feature from "ol/Feature";
import Map from "ol/Map";
import Point from "ol/geom/Point";
import View from "ol/View";
import { Circle as CircleStyle, Stroke, Style } from "ol/style";
import { OSM, Vector as VectorSource } from "ol/source";
import { Tile as TileLayer, Vector as VectorLayer } from "ol/layer";
import { easeOut } from "ol/easing";
import { fromLonLat } from "ol/proj";
import { getVectorContext } from "ol/render";
import { unByKey } from "ol/Observable";

const LOCATION_URL = "/location";
const FETCH_INTERVAL = 3000;
const FLASH_DURATION = 3000;
const maxZoom = 15;

const tileLayer = new TileLayer({
  source: new OSM({
    wrapX: false,
  }),
});

const source = new VectorSource({
  wrapX: false,
});

const vector = new VectorLayer({
  source: source,
});

const map = new Map({
  layers: [tileLayer, vector],
  target: "map",
  view: new View({
    center: [0, 0],
    zoom: 2,
  }),
});

function flash(feature) {
  const start = Date.now();
  const flashGeom = feature.getGeometry().clone();
  const listenerKey = tileLayer.on("postrender", animate);

  function animate(event) {
    const frameState = event.frameState;
    const elapsed = frameState.time - start;
    if (elapsed >= FLASH_DURATION) {
      unByKey(listenerKey);
      return;
    }
    const vectorContext = getVectorContext(event);
    const elapsedRatio = elapsed / FLASH_DURATION;
    // radius will be 5 at start and 30 at end.
    const radius = easeOut(elapsedRatio) * 25 + 5;
    const opacity = easeOut(1 - elapsedRatio);

    const style = new Style({
      image: new CircleStyle({
        radius: radius,
        stroke: new Stroke({
          color: "rgba(255, 0, 0, " + opacity + ")",
          width: 0.25 + opacity,
        }),
      }),
    });

    vectorContext.setStyle(style);
    vectorContext.drawGeometry(flashGeom);
    // tell OpenLayers to continue postrender animation
    map.render();
  }
}

source.on("addfeature", (e) => {
  flash(e.feature);
});

window.setInterval(fetchCoordinates, FETCH_INTERVAL);

function fetchCoordinates() {
  fetch(LOCATION_URL)
    .then((response) => response.json())
    .then(displayCoordinates)
    .catch(() => {
      console.log("Invalid data.");
    });
}

function displayCoordinates(coordinates) {

  const lon = coordinates.longitude;
  const lat = coordinates.latitude;

  if (map.getView().getZoom() < maxZoom) {
    zoomIn();
    panTo(lon, lat);
  } else {
    addMarker(lon, lat);
  }
}

function zoomIn() {
  map.getView().animate({
    zoom: maxZoom,
    duration: 1000,
  });
}

function panTo(lon, lat) {
  map.getView().animate({
    center: fromLonLat([lon, lat]),
    duration: 1000,
  });
}

function addMarker(lon, lat) {
  const point = new Point(fromLonLat([lon, lat]));
  const feature = new Feature(point);
  source.addFeature(feature);
  console.log("Hej!");
}
