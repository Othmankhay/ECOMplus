import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {NgFor, NgIf} from '@angular/common';
import {Router} from '@angular/router';

@Component({
  selector: 'app-customers',
  standalone: true,
  imports: [NgIf, NgFor],
  templateUrl: './customers.html',
  styleUrl: './customers.css',
})
export class Customers implements OnInit {

  customers: any;

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit(): void {
    this.http.get("http://localhost:8888/api/customers").subscribe(
      {
        next : (data)=>{
          this.customers=data;
        },
        error : (err)=>{}
      }
    );
  }

  getOrders(c: any) {
    this.router.navigateByUrl("/orders/"+c.id);


  }
}
