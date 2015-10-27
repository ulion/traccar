/*
 * Copyright 2015 Anton Tananaev (anton.tananaev@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

Ext.define('Traccar.view.MapController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.map',

    config: {
        listen: {
            controller: {
                '*': {
                    reportShow: 'reportShow',
                    reportClear: 'reportClear',
                    selectDevice: 'selectDevice',
                    selectReport: 'selectReport'
                }
            }
        }
    },

    init: function () {
        this.liveData = {};
        this.update(true);
    },

    update: function (first) {
        Ext.Ajax.request({
            scope: this,
            url: '/api/async',
            params: {
                first: first
            },
            success: function (response) {
                var data = Ext.decode(response.responseText).data;

                var i;
                for (i = 0; i < data.length; i++) {

                    var store = Ext.getStore('LatestPositions');
                    var deviceStore = Ext.getStore('Devices');

                    var found = store.query('deviceId', data[i].deviceId);
                    if (found.getCount() > 0) {
                        var t = new Date(data[i].fixTime);
                        if (found.first().get('fixTime').getTime() <= t.getTime())
                            found.first().set(data[i]);
                    } else {
                        store.add(Ext.create('Traccar.model.Position', data[i]));
                    }

                    var geometry = new ol.geom.Point(ol.proj.fromLonLat([
                        data[i].longitude,
                        data[i].latitude
                    ]));

                    if (data[i].deviceId in this.liveData) {
                        this.liveData[data[i].deviceId].setGeometry(geometry);
                        var style = this.getMarkerStyle(Traccar.Style.mapLiveRadius, Traccar.Style.mapLiveColor, data[i].course, name);
                        this.liveData[data[i].deviceId].setStyle(style);
                    } else {
                        var name = deviceStore.query('id', data[i].deviceId).first().get('name');

                        var style = this.getMarkerStyle(Traccar.Style.mapLiveRadius, Traccar.Style.mapLiveColor, data[i].course, name);
                        var marker = new ol.Feature({
                            geometry: geometry,
                            originalStyle: style
                        });
                        marker.setStyle(style);
                        this.getView().vectorSource.addFeature(marker);
                        this.liveData[data[i].deviceId] = marker;
                    }
                }

                this.update(false);
            },
            failure: function () {
                // TODO: error
            }
        });
    },

    getLineStyle: function () {
        return new ol.style.Style({
            stroke: new ol.style.Stroke({
                color: Traccar.Style.mapStrokeColor,
                width: Traccar.Style.mapRouteWidth
            })
        });
    },

    getMarkerStyle: function (radius, color, rotation, text) {
        return new ol.style.Style({
            image: new ol.style.RegularShape({
                points: 3,
                radius: radius,
                fill: new ol.style.Fill({
                    color: color
                }),
                stroke: new ol.style.Stroke({
                    color: Traccar.Style.mapStrokeColor,
                    width: Traccar.Style.mapMarkerStroke
                }),
                rotation: rotation * Math.PI / 180
            }),
            text: new ol.style.Text({
                textBaseline: 'bottom',
                text: text,
                fill: new ol.style.Fill({
                    color: '#000'
                }),
                stroke: new ol.style.Stroke({
                    color: '#FFF',
                    width: 2
                }),
                offsetY: -12,
                font : 'bold 12px sans-serif'
            })
        });
    },

    reportShow: function () {
        this.reportClear();

        var vectorSource = this.getView().vectorSource;

        var data = Ext.getStore('Positions').getData();

        var index;
        var positions = [];
        this.reportRoutePoints = {};

        for (index = 0; index < data.getCount(); index++) {
            var point = ol.proj.fromLonLat([
                data.getAt(index).data.longitude,
                data.getAt(index).data.latitude
            ]);
            positions.push(point);

            var style = this.getMarkerStyle(Traccar.Style.mapReportRadius, Traccar.Style.mapReportColor, data.getAt(index).data.course);
            var feature = new ol.Feature({
                geometry: new ol.geom.Point(positions[index]),
                originalStyle: style
            });
            feature.setStyle(style);
            this.reportRoutePoints[data.getAt(index).get('id')] = feature;
        }

        this.reportRoute = new ol.Feature({
            geometry: new ol.geom.LineString(positions)
        });
        this.reportRoute.setStyle(this.getLineStyle());
        vectorSource.addFeature(this.reportRoute);

        for (var key in this.reportRoutePoints) {
            if (this.reportRoutePoints.hasOwnProperty(key)) {
                vectorSource.addFeature(this.reportRoutePoints[key]);
            }
        }
    },

    reportClear: function () {
        var vectorSource = this.getView().vectorSource;

        if (this.reportRoute !== undefined) {
            vectorSource.removeFeature(this.reportRoute);
            this.reportRoute = undefined;
        }

        if (this.reportRoutePoints !== undefined) {
            for (var key in this.reportRoutePoints) {
                if (this.reportRoutePoints.hasOwnProperty(key)) {
                    vectorSource.removeFeature(this.reportRoutePoints[key]);
                }
            }
            this.reportRoutePoints = {};
        }
    },

    selectPosition: function (feature) {
        if (this.currentFeature !== undefined) {
            this.currentFeature.setStyle(this.currentFeature.get('originalStyle'));
        }

        if (feature !== undefined) {
            var name = feature.getStyle().getText().getText();
            feature.setStyle(this.getMarkerStyle(Traccar.Style.mapSelectRadius, Traccar.Style.mapSelectColor, 0, name));

            var pan = ol.animation.pan({
                duration: Traccar.Style.mapDelay,
                source: this.getView().mapView.getCenter()
            });
            this.getView().map.beforeRender(pan);
            this.getView().mapView.setCenter(feature.getGeometry().getCoordinates());
        }

        this.currentFeature = feature;
    },

    selectDevice: function (device) {
        this.selectPosition(this.liveData[device.get('id')]);
    },

    selectReport: function (position) {
        if (this.reportRoutePoints[position.get('id')] !== undefined) {
            this.selectPosition(this.reportRoutePoints[position.get('id')]);
        }
    }

});
