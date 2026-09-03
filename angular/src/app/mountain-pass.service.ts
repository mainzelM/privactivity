import {Injectable} from '@angular/core';

import {environment} from '../environments/environment';
import {Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";

export interface LatLon {
    lat: number;
    lon: number;
}

export interface MountainPass {
    name: string;
    country: string;
    summitLatLon: LatLon;
}

export interface CrossedPasses {
    crossedPasses: MountainPass[];
}

export interface PassCrossing {
    passName: string;
    passCountry: string;
    firstCrossingActivityId: number;
    firstCrossingDate: string;
    lastCrossingActivityId: number;
    lastCrossingDate: string;
}

export interface AllCrossedPasses {
    passCrossings: PassCrossing[];
}

@Injectable({
    providedIn: 'root'
})
export class MountainPassService {
    private baseUrl = environment.apiUrl + '/passes';

    constructor(private http: HttpClient) {
    }

    getCrossedPasses(activityId: number): Observable<CrossedPasses> {
        return this.http.get<CrossedPasses>(this.baseUrl + '/' + activityId);
    }

    getAllCrossedPasses(): Observable<AllCrossedPasses> {
        return this.http.get<AllCrossedPasses>(this.baseUrl + '/all');
    }
}
