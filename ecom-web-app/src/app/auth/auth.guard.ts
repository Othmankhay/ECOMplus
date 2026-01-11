import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { KeycloakService } from 'keycloak-angular';

export const authGuard: CanActivateFn = async (route, state) => {
  const keycloakService = inject(KeycloakService);
  const router = inject(Router);

  const isLoggedIn = keycloakService.isLoggedIn();
  
  if (!isLoggedIn) {
    await keycloakService.login({
      redirectUri: window.location.origin + state.url
    });
    return false;
  }

  const requiredRoles = route.data?.['roles'] as string[];
  
  if (requiredRoles && requiredRoles.length > 0) {
    const hasRequiredRole = requiredRoles.some(role => 
      keycloakService.isUserInRole(role)
    );
    
    if (!hasRequiredRole) {
      router.navigate(['/unauthorized']);
      return false;
    }
  }

  return true;
};

export const adminGuard: CanActivateFn = async (route, state) => {
  const keycloakService = inject(KeycloakService);
  const router = inject(Router);

  const isLoggedIn = keycloakService.isLoggedIn();
  
  if (!isLoggedIn) {
    await keycloakService.login({
      redirectUri: window.location.origin + state.url
    });
    return false;
  }

  if (!keycloakService.isUserInRole('ADMIN')) {
    router.navigate(['/unauthorized']);
    return false;
  }

  return true;
};
