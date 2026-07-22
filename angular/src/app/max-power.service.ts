import {Injectable} from '@angular/core';

import {environment} from '../environments/environment';
import {MaxPower} from "./maxPower";
import {Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";

@Injectable({
    providedIn: 'root'
})
export class MaxPowerService {
    constructor(private http: HttpClient) {
    }

    private baseUrl = environment.apiUrl + '/maxpower';

    getMaxPowerAllActivities(): Observable<MaxPower> {
        return this.http.get<MaxPower>(this.baseUrl);
    }

    getMaxPowerOfActivity(activityId: number): Observable<MaxPower> {
        return this.http.get<MaxPower>(this.baseUrl + '/' + activityId);
    }
}