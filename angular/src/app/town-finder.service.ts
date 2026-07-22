import {Injectable} from '@angular/core';

import {environment} from '../environments/environment';
import {Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";

@Injectable({
    providedIn: 'root'
})
export class TownFinderService {
    constructor(private http: HttpClient) {
    }

    private baseUrl = environment.apiUrl + '/townfinder';


    getTownsOfActivity(activityId: number): Observable<string[]> {
        return this.http.get<string[]>(this.baseUrl + '/' + activityId);
    }

    getTownsOfActivityBounds(activityId: number): Observable<string[]> {
        return this.http.get<string[]>(this.baseUrl + '/' + activityId + '/bounds');
    }

    getTownsOfActivityCorners(activityId: number): Observable<string[]> {
        return this.http.get<string[]>(this.baseUrl + '/' + activityId + '/corners');
    }
}