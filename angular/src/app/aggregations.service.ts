import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {environment} from '../environments/environment';

import {EddigtionChart, MovingSum, TotalAggregation} from "./aggregations";
import {Observable} from "rxjs";

@Injectable({
    providedIn: 'root'
})
export class AggregationsService {
    private baseUrl = environment.apiUrl + '/aggregations';
    private baseUrlYTD = environment.apiUrl + '/aggregations/ytd';
    private movingSumUrl = environment.apiUrl + '/aggregations/moving-sum';
    private eddingtonChartUrl = environment.apiUrl + '/aggregations/eddington-chart';

    constructor(private http: HttpClient) {
    }

    getAll(months: string = '', isYTD: boolean = false): Observable<TotalAggregation> {
        let params = new HttpParams();
        if (months) {
            params = params.set('months', months);
        }
        return this.http.get<TotalAggregation>(isYTD ? this.baseUrlYTD : this.baseUrl);
    }

    getMovingSum(numDays: number = 365): Observable<MovingSum> {
        const params = new HttpParams().set('numDays', numDays);
        return this.http.get<MovingSum>(this.movingSumUrl, {params});
    }

    getEddingtonChart(): Observable<EddigtionChart> {
        return this.http.get<EddigtionChart>(this.eddingtonChartUrl);
    }
}