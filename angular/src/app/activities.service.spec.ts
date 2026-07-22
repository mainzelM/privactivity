import {TestBed} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';

import {ActivitiesService} from './activities.service';
import {environment} from '../environments/environment';

describe('ActivitiesService', () => {
    let service: ActivitiesService;
    let httpTestingController: HttpTestingController;

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [
                provideHttpClient(),
                provideHttpClientTesting()
            ]
        });

        service = TestBed.inject(ActivitiesService);
        httpTestingController = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpTestingController.verify();
    });

    it('should send title updates to the activity title endpoint', () => {
        service.changeTitle(42, 'New Title').subscribe();

        const request = httpTestingController.expectOne(`${environment.apiUrl}/activities/42/title`);
        expect(request.request.method).toBe('PUT');
        expect(request.request.body).toEqual({title: 'New Title'});

        request.flush(null);
    });
});
