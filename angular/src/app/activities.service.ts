import {Injectable} from '@angular/core';
import {Activity} from "./activity";
import {ActivityFilterRequest} from "./activity-filter-request";
import {environment} from "../environments/environment";
import {HttpClient, HttpParams, HttpResponse} from "@angular/common/http";
import {Observable} from "rxjs";

export interface CsvExportConfigRequest {
    latLon: boolean;
    speed: boolean;
    power: boolean;
    heartRate: boolean;
    cadence: boolean;
    altitude: boolean;
    lap: boolean;
    secondsToAverage: number;
}

export interface ChangeTitleRequest {
    title: string;
}

@Injectable({
    providedIn: 'root'
})
export class ActivitiesService {

    constructor(private http: HttpClient) {
    }

    private url = environment.apiUrl + '/activities';
    private geoJsonurl = environment.apiUrl + '/geojson';

    getAllActivities(filter?: ActivityFilterRequest): Observable<Activity[]> {
        let params = new HttpParams();
        if (filter) {
            if (filter.includeWaypoints !== undefined) {
                params = params.set('includeWaypoints', filter.includeWaypoints);
            }
            if (filter.minDistance !== undefined && filter.minDistance !== null) {
                params = params.set('minDistance', filter.minDistance);
            }
            if (filter.maxDistance !== undefined && filter.maxDistance !== null) {
                params = params.set('maxDistance', filter.maxDistance);
            }
            if (filter.minAscent !== undefined && filter.minAscent !== null) {
                params = params.set('minAscent', filter.minAscent);
            }
            if (filter.maxAscent !== undefined && filter.maxAscent !== null) {
                params = params.set('maxAscent', filter.maxAscent);
            }
            if (filter.tileId !== undefined && filter.tileId !== null && filter.tileId.trim() !== '') {
                params = params.set('tileId', filter.tileId);
            }
            if (filter.lat !== undefined && filter.lat !== null) {
                params = params.set('lat', filter.lat);
            }
            if (filter.lon !== undefined && filter.lon !== null) {
                params = params.set('lon', filter.lon);
            }
            if (filter.radius !== undefined && filter.radius !== null) {
                params = params.set('radius', filter.radius);
            }
            if (filter.title !== undefined && filter.title !== null && filter.title.trim() !== '') {
                params = params.set('title', filter.title);
            }
            if (filter.sortBy !== undefined && filter.sortBy !== null) {
                params = params.set('sortBy', filter.sortBy);
            }
            if (filter.sortDirection !== undefined && filter.sortDirection !== null) {
                params = params.set('sortDirection', filter.sortDirection);
            }
        }
        return this.http.get<Activity[]>(this.url, {params});
    }

    getActivity(id: number): Observable<Activity> {
        return this.http.get<Activity>(`${this.url}/${id}`);
    }

    getGeoJSON(id: number): Observable<any> {
        return this.http.get<any>(`${this.geoJsonurl}/${id}`);
    }

    exportActivityAsCsv(id: number, config: CsvExportConfigRequest): Observable<HttpResponse<Blob>> {
        return this.http.post(`${this.url}/${id}/export.csv`, config, {observe: 'response', responseType: 'blob'});
    }

    changeTitle(id: number, title: string): Observable<void> {
        const request: ChangeTitleRequest = {title};
        return this.http.put<void>(`${this.url}/${id}/title`, request);
    }
}
