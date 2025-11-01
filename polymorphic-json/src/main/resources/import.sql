insert into book (
    coupons,
    isbn,
    id
)
values (
  '[
       {
           "name":"PPP",
           "amount":4.99,
           "type":"discount.coupon.amount"
    },
           {
           "name":"Black Friday",
           "percentage":0.02,
           "type":"discount.coupon.percentage"
    }
  ]'::json,
  '978-9730228236',
  1
);

alter sequence book_SEQ restart with 2;