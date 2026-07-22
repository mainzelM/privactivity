import {Routes} from '@angular/router';
import {ActivitiesComponent} from "./activities/activities.component";
import {ActivityDetailsComponent} from "./activity-details/activity-details.component";
import {AggregationsComponent} from "./aggregations/aggregations.component";
import {MaxPowerComponent} from "./max-power/max-power.component";
import {LoginComponent} from "./login/login.component";
import {authGuard} from "./auth/auth-guard.service";
import {LogoutComponent} from "./logout/logout.component";
import {AdminComponent} from "./admin/admin.component";
import {TilesComponent} from "./tiles/tiles.component";

const routeConfig: Routes = [
    {
        path: '',
        pathMatch: 'full',
        component: ActivitiesComponent,
        title: 'Activities',
        canActivate: [authGuard],
        data: {role: 'ROLE_USER'}
    },
    {
        path: 'activity-details/:id',
        component: ActivityDetailsComponent,
        title: 'Activity details',
        canActivate: [authGuard],
        data: {role: 'ROLE_USER'}
    },
    {
        path: 'aggregations',
        component: AggregationsComponent,
        title: 'Aggregations',
        canActivate: [authGuard],
        data: {role: 'ROLE_USER'}
    },
    {
        path: 'max-power',
        component: MaxPowerComponent,
        title: 'Max Power',
        canActivate: [authGuard],
        data: {role: 'ROLE_USER'}
    },
    {
        path: 'tiles',
        component: TilesComponent,
        title: 'Tiles',
        canActivate: [authGuard],
        data: {role: 'ROLE_USER'}
    },
    {
        path: 'login',
        component: LoginComponent,
        title: 'Login',
    },
    {
        path: 'logout',
        component: LogoutComponent,
        title: 'Logout',
    },
    {
        path: 'admin',
        component: AdminComponent,
        title: 'Administration',
        canActivate: [authGuard],
        data: {role: 'ROLE_ADMIN'}
    },
];
export default routeConfig;