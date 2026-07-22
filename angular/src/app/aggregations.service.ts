import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {environment} from '../environments/environment';

import {TotalAggregation} from "./aggregations";
import {Observable} from "rxjs";

@Injectable({
    providedIn: 'root'
})
export class AggregationsService {
    private baseUrl = environment.apiUrl + '/aggregations';
    private baseUrlYTD = environment.apiUrl + '/aggregations/ytd';

    constructor(private http: HttpClient) {
    }

    getAll(months: string = '', isYTD: boolean = false): Observable<TotalAggregation> {
        let params = new HttpParams();
        if (months) {
            params = params.set('months', months);
        }
        return this.http.get<TotalAggregation>(isYTD ? this.baseUrlYTD : this.baseUrl);
    }
}