import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { KeycloakService } from 'keycloak-angular';
import { Observable, from, switchMap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private keycloakService = inject(KeycloakService);
  private http = inject(HttpClient);

  isLoggedIn(): boolean {
    return this.keycloakService.isLoggedIn();
  }

  getUsername(): string {
    return this.keycloakService.getUsername();
  }

  getToken(): Promise<string> {
    return this.keycloakService.getToken();
  }

  getUserRoles(): string[] {
    return this.keycloakService.getUserRoles();
  }

  hasRole(role: string): boolean {
    return this.keycloakService.isUserInRole(role);
  }

  login(): void {
    this.keycloakService.login();
  }

  logout(): void {
    this.keycloakService.logout(window.location.origin);
  }

  async getUserProfile() {
    return this.keycloakService.loadUserProfile();
  }

  async getAuthHeaders(): Promise<HttpHeaders> {
    const token = await this.getToken();
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  authenticatedGet<T>(url: string): Observable<T> {
    return from(this.getAuthHeaders()).pipe(
      switchMap(headers => this.http.get<T>(url, { headers }))
    );
  }

  authenticatedPost<T>(url: string, body: any): Observable<T> {
    return from(this.getAuthHeaders()).pipe(
      switchMap(headers => this.http.post<T>(url, body, { headers }))
    );
  }

  authenticatedPut<T>(url: string, body: any): Observable<T> {
    return from(this.getAuthHeaders()).pipe(
      switchMap(headers => this.http.put<T>(url, body, { headers }))
    );
  }

  authenticatedDelete<T>(url: string): Observable<T> {
    return from(this.getAuthHeaders()).pipe(
      switchMap(headers => this.http.delete<T>(url, { headers }))
    );
  }
}
