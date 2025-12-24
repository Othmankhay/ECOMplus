import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import {NgForOf, NgIf} from '@angular/common';

@Component({
  selector: 'app-orders',
  standalone: true,
  templateUrl: './orders.html',
  styleUrls: ['./orders.css'],
  imports: [
    NgForOf,
    NgIf
  ],
})
export class Orders implements OnInit {

  orders: any;
  customerId!: number;

  constructor(private http: HttpClient, private router: Router, private route: ActivatedRoute) {
    this.customerId = this.route.snapshot.params['customerId'];
  }

  ngOnInit(): void {
    this.http.get(`http://localhost:8088/BILLING-SERVICE/api/bills/search/byCustomerId?projection=fullBill&customerId=${this.customerId}`)
      .subscribe({
        next: (data) => {
          this.orders = data;
        },
        error: (err) => {
          console.log(err);
        }
      });
  }

  getOrders(order: any) {
    this.router.navigateByUrl("/orders/" + order.id);
  }

  getOrderDetails(order: any) {

    this.router.navigateByUrl("/order-details/" + order.id);

  }
}
