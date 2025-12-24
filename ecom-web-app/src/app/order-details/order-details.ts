import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { NgForOf, NgIf, DatePipe, DecimalPipe } from '@angular/common';

@Component({
  selector: 'app-order-details',
  imports: [
    NgForOf,
    NgIf,
    DatePipe,
    DecimalPipe
  ],
  templateUrl: './order-details.html',
  styleUrl: './order-details.css',
})
export class OrderDetails implements OnInit {

  orderDetails: any;
  orderId!: number;


  totalAmount: number = 0;

  constructor(
    private http: HttpClient,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.orderId = this.route.snapshot.params['orderId'];
  }

  ngOnInit(): void {
    this.http.get(
      `http://localhost:8088/BILLING-SERVICE/fullBill/${this.orderId}`
    ).subscribe({
      next: (data: any) => {
        this.orderDetails = data;
        this.totalAmount = this.calculateTotal(data.productItems);
      },
      error: (err) => {
        console.log(err);
      }
    });
  }

  calculateTotal(items: any[]): number {
    if (!items) return 0;
    return items.reduce(
      (sum, item) => sum + (item.quantity * item.unitPrice),
      0
    );
  }

}
