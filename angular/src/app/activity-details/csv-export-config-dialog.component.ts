import {Component, Inject} from '@angular/core';

import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {CsvExportConfigRequest} from '../activities.service';

@Component({
    selector: 'app-csv-export-config-dialog',
    standalone: true,
    imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule
],
    templateUrl: './csv-export-config-dialog.component.html',
    styleUrls: ['./csv-export-config-dialog.component.scss']
})
export class CsvExportConfigDialogComponent {
    readonly form = this.formBuilder.nonNullable.group({
        latLon: [this.data.latLon],
        speed: [this.data.speed],
        power: [this.data.power],
        heartRate: [this.data.heartRate],
        cadence: [this.data.cadence],
        altitude: [this.data.altitude],
        lap: [this.data.lap],
        secondsToAverage: [this.data.secondsToAverage, [Validators.required, Validators.min(1)]]
    });

    constructor(
        private formBuilder: FormBuilder,
        private dialogRef: MatDialogRef<CsvExportConfigDialogComponent, CsvExportConfigRequest>,
        @Inject(MAT_DIALOG_DATA) public data: CsvExportConfigRequest
    ) {
    }

    submit(): void {
        if (this.form.invalid) {
            this.form.markAllAsTouched();
            return;
        }
        this.dialogRef.close(this.form.getRawValue());
    }
}