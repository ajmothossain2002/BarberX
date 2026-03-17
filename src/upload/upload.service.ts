import { Injectable } from '@nestjs/common';
import { v2 as cloudinary } from 'cloudinary';
import { ConfigService } from '@nestjs/config';

@Injectable()
export class UploadService {
    constructor(private config: ConfigService) {
        cloudinary.config({
            cloud_name: this.config.get<string>('CLOUDINARY_CLOUD_NAME'),
            api_key: this.config.get<string>('CLOUDINARY_API_KEY'),
            api_secret: this.config.get<string>('CLOUDINARY_API_SECRET'),
        });
    }

    getUploadSignature() {
        const timestamp = Math.round(new Date().getTime() / 1000);
        const params = { timestamp };
        const signature = cloudinary.utils.api_sign_request(
            params,
            this.config.get<string>('CLOUDINARY_API_SECRET') as string,
        );
        return {
            timestamp,
            signature,
            apiKey: this.config.get('CLOUDINARY_API_KEY'),
            cloudName: this.config.get('CLOUDINARY_CLOUD_NAME'),
        };
    }
}
